package com.researchassistant.backend.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AIService {

    private final RestClient restClient;

    public AIService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    /**
     * Generate each paper section separately. Smaller, focused requests are
     * more reliable on llama3.2:3b than asking the model for a full paper in
     * one response, where later sections are often truncated or very short.
     *
     * The response keys are intentionally kept compatible with the frontend.
     */
    public Map<String, String> generateResearchPaper(String topic) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("topic", topic);

        result.put("title", generateSection(topic, "TITLE",
                "Create exactly ONE concise academic research paper title containing 10 to 15 words inclusive (aim for 12). Return only the title on one line, with no Title label, quotation marks, explanation, or alternative titles. Avoid filler. Count the words before responding and revise the title until it has 10–15 words.",
                60));
        result.put("abstract", generateSection(topic, "ABSTRACT",
                "Write a complete academic abstract of 180–220 words. Include the research context, problem, aim, proposed approach, expected contribution, and limitations. Do not claim that experiments or results occurred if none were supplied. Clearly frame outcomes as expected when appropriate.",
                450));
        result.put("keywords", generateSection(topic, "KEYWORDS",
                "Return 6–8 specific academic keywords separated by commas. Return only the keywords.",
                100));
        result.put("introduction", generateSection(topic, "INTRODUCTION",
                "Write 450–600 words in several coherent paragraphs. Cover background and context, the problem statement, why the problem matters, the research gap (describe cautiously rather than inventing a verified gap), aim, 3–5 objectives, research questions where suitable, scope, significance, and a brief roadmap of the paper. Avoid repetitive filler.",
                1100));
        result.put("literatureReview", generateSection(topic, "LITERATURE REVIEW",
                "Write 500–700 words in several thematic paragraphs. Explain key concepts, summarize relevant established approaches and research themes, compare their strengths and limitations, identify open questions, and explain how the proposed study fits. Do not invent author names, paper titles, publication years, DOIs, or citations. If no source list is provided, explicitly describe this as a thematic overview that needs verification against real scholarly sources.",
                1300));
        result.put("methodology", generateSection(topic, "METHODOLOGY",
                "Write 400–550 words describing a credible proposed methodology: research design, data/materials needed, sampling or selection criteria where applicable, procedure, tools/analysis method, evaluation criteria, ethical/privacy considerations, and limitations. Adapt to the topic. Do not pretend that data were collected, participants recruited, or experiments completed. Use future tense or clearly label the method as proposed when appropriate.",
                1050));
        result.put("conclusion", generateSection(topic, "CONCLUSION",
                "Write 250–350 words. Synthesize the paper's main argument, relate it to the stated aim, describe potential contributions and practical implications, acknowledge limitations, and suggest specific future work. Do not introduce unsupported results or citations. Avoid merely repeating the abstract.",
                700));

        return result;
    }

    public Map<String, String> generateAbstractAndKeywords(String topic) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("topic", topic);
        result.put("abstract", generateSection(topic, "ABSTRACT",
                "Write a complete academic abstract of approximately 180–220 words covering context, problem, aim, proposed approach, expected contribution and limitations. Do not fabricate results; label expected outcomes as expected.",
                450));
        result.put("keywords", generateSection(topic, "KEYWORDS",
                "Return 6–8 specific academic keywords separated by commas. Return only the keywords.",
                100));
        return result;
    }

    public String improveSection(
            String topic,
            String section,
            String content,
            String instruction) {

        String prompt = """
                You are a careful academic writing assistant.

                Research topic: %s
                Section: %s
                User instruction: %s

                Revise the supplied text in detailed, coherent academic English.
                Preserve its intended meaning and relevant details. Improve
                structure, reasoning, clarity and transitions. Do not add
                fake citations, references, statistics, data or findings.
                Return only the revised section content.
                """.formatted(topic, section, instruction)
                + "\n\nOriginal section:\n" + content;

        return callOllama(prompt, 1600).trim();
    }

    private String generateSection(String topic, String section,
                                   String instructions, int maxTokens) {
        String prompt = """
                You are an experienced academic research-writing assistant.
                Write a detailed academic research-paper section, not a brief overview.

                Research topic: %s
                Section: %s

                Follow these instructions carefully:
                %s

                Write complete paragraphs with explanation, examples relevant to
                the topic, critical analysis, transitions, and appropriate nuance.
                Do not use bullet points as a substitute for the requested prose.
                Never invent sources, author names, citations, statistics, experiments,
                participant data, or research findings. Clearly label proposed methods
                and expected outcomes; do not claim work was performed if it was not.
                Return only the section content. Do not include a heading, preamble,
                word count, or commentary.
                """.formatted(topic, section, instructions);

        String generated = callOllama(prompt, maxTokens).trim();
        if (generated.isBlank()) {
            throw new IllegalStateException(
                    "Ollama returned empty content for section: " + section);
        }

        // Keep local generation responsive: at most one expansion request per
        // section, and only when the first response is clearly too short.
        int minimumWords = switch (section) {
            case "ABSTRACT" -> 140;
            case "INTRODUCTION" -> 350;
            case "LITERATURE REVIEW" -> 400;
            case "METHODOLOGY" -> 300;
            case "CONCLUSION" -> 200;
            default -> 0;
        };

        for (int attempt = 0; attempt < 1
                && minimumWords > 0
                && countWords(generated) < minimumWords; attempt++) {
            String expansionPrompt = """
                    Expand the following academic research-paper section substantially.
                    Topic: %s
                    Section: %s
                    Current word count: %d
                    Minimum target: %d words.

                    Preserve useful content, then add relevant explanation, analysis,
                    examples, implications, limitations, and transitions in full paragraphs.
                    Do not repeat sentences merely to increase length. Do not invent
                    citations, sources, data, statistics, or findings. Keep proposed work
                    clearly distinguished from completed research. Return only the
                    complete expanded section, not notes or a word-count report.

                    Existing section:
                    %s
                    """.formatted(topic, section, countWords(generated),
                    minimumWords, generated);
            String expanded = callOllama(expansionPrompt, maxTokens).trim();
            if (expanded.isBlank()) {
                break;
            }
            // Keep the more substantial response if the model unexpectedly shortens it.
            if (countWords(expanded) > countWords(generated)) {
                generated = expanded;
            } else {
                break;
            }
        }
        return generated;
    }

    private int countWords(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    private String callOllama(String prompt, int maxTokens) {
        JsonNode response = restClient.post()
                .uri("/api/generate")
                .body(Map.of(
                        "model", "llama3.2:3b",
                        "prompt", prompt,
                        "stream", false,
                        "options", Map.of(
                                "num_predict", maxTokens,
                                "temperature", 0.4
                        )
                ))
                .retrieve()
                .body(JsonNode.class);

        if (response == null || response.path("response").isMissingNode()) {
            throw new IllegalStateException("Ollama returned an empty response");
        }

        String text = response.path("response").asText();
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("Ollama returned blank text");
        }
        return text;
    }
}
