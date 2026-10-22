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
                "Create one precise, informative academic research-paper title. Return only the title, without a heading or quotation marks. Maximum 20 words.",
                100));
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
                Produce publication-style draft prose for the requested section.

                Research topic: %s
                Section: %s

                Requirements:
                - Follow the section-specific instructions below.
                - Write substantive, well-organized academic prose, not a short summary.
                - Use clear paragraphs, precise terminology, and logical transitions.
                - Avoid repeating the same point to inflate length.
                - Never invent sources, author names, citations, statistics,
                  experiments, participant data, or research findings.
                - If evidence or source material is unavailable, state the
                  limitation and distinguish established background from
                  proposed ideas or expected outcomes.
                - Return only the section content, without the section heading,
                  preamble, word count, or commentary.

                Section-specific instructions:
                %s
                """.formatted(topic, section, instructions);

        String generated = callOllama(prompt, maxTokens).trim();
        if (generated.isBlank()) {
            throw new IllegalStateException(
                    "Ollama returned empty content for section: " + section);
        }
        return generated;
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
