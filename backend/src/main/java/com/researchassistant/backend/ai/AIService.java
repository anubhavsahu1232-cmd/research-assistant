
package com.researchassistant.backend.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AIService {

    private final RestClient restClient;

    public AIService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    public Map<String, String> generateResearchPaper(String topic) {

        String prompt = """
                You are an academic research assistant.

                Write an academic research paper draft about:
                %s

                Return the paper using exactly these headings,
                each on a separate line:
                TITLE:
                ABSTRACT:
                KEYWORDS:
                INTRODUCTION:
                LITERATURE REVIEW:
                METHODOLOGY:
                CONCLUSION:

                Write clear academic English under each heading.
                Do not omit any section.
                Do not invent citations, references, statistics,
                or research findings.
                """.formatted(topic);

        String paper = callOllama(prompt);

        Map<String, String> result = new LinkedHashMap<>();
        result.put("topic", topic);

        Pattern pattern = Pattern.compile(
                "(?im)^\\s*(?:\\*{1,3}\\s*)?" +
                "(TITLE|ABSTRACT|KEYWORDS|INTRODUCTION|" +
                "LITERATURE\\s+REVIEW|METHODOLOGY|CONCLUSION)" +
                "\\s*:?\\s*(?:\\*{1,3})?\\s*$"
        );

        Matcher matcher = pattern.matcher(paper);

        String currentSection = null;
        int contentStart = 0;

        while (matcher.find()) {
            if (currentSection != null) {
                result.put(
                        currentSection,
                        paper.substring(contentStart, matcher.start()).trim()
                );
            }

            currentSection = normalizeHeading(matcher.group(1));
            contentStart = matcher.end();
        }

        if (currentSection != null) {
            result.put(
                    currentSection,
                    paper.substring(contentStart).trim()
            );
        }

        return result;
    }

    public Map<String, String> generateAbstractAndKeywords(String topic) {

        String prompt = """
                You are an academic research writing assistant.

                Research topic: %s

                IMPORTANT: Return these two headings on separate lines,
                exactly as written:
                ABSTRACT:
                [abstract text]

                KEYWORDS:
                [5 to 8 keywords separated by commas]

                Write an academic abstract of approximately 150 words.
                Do not invent research results, statistics, or citations.
                Treat this as a proposed research draft if actual findings
                are unavailable.
                """.formatted(topic);

        String generated = callOllama(prompt);

        Map<String, String> result = new LinkedHashMap<>();
        result.put("topic", topic);

        // Recognizes headings even if Ollama puts both on one line.
        Pattern pattern = Pattern.compile(
                "(?i)\\b(ABSTRACT|KEYWORDS)\\s*:"
        );

        Matcher matcher = pattern.matcher(generated);

        String currentSection = null;
        int contentStart = 0;

        while (matcher.find()) {
            if (currentSection != null) {
                result.put(
                        currentSection,
                        generated.substring(contentStart, matcher.start()).trim()
                );
            }

            currentSection = matcher.group(1).toLowerCase();
            contentStart = matcher.end();
        }

        if (currentSection != null) {
            result.put(
                    currentSection,
                    generated.substring(contentStart).trim()
            );
        }

        result.putIfAbsent("abstract", "");
        result.putIfAbsent("keywords", "");

        // Remove leftover Markdown markers from extracted content.
        result.replaceAll(
                (key, value) -> value == null
                        ? ""
                        : value.replaceAll("(?m)^\\s*\\*{1,3}\\s*", "")
                               .trim()
        );

        return result;
    }

    public String improveSection(
            String topic,
            String section,
            String content,
            String instruction) {

        String prompt = """
                You are an academic writing assistant.

                Research topic: %s
                Section: %s
                User instruction: %s

                Improve the section according to the instruction.
                Preserve its original meaning where possible.
                Use clear academic English.
                Do not invent citations, statistics, or research findings.
                Return only the improved section content.

                Original section:
                %s
                """.formatted(topic, section, instruction, content);

        return callOllama(prompt).trim();
    }

    private String callOllama(String prompt) {

        JsonNode response = restClient.post()
                .uri("/api/generate")
                .body(Map.of(
                        "model", "llama3.2:3b",
                        "prompt", prompt,
                        "stream", false
                ))
                .retrieve()
                .body(JsonNode.class);

        if (response == null
                || response.path("response").isMissingNode()) {
            throw new IllegalStateException(
                    "Ollama returned an empty response"
            );
        }

        return response.path("response").asText();
    }

    private String normalizeHeading(String heading) {

        return switch (heading.toUpperCase().replaceAll("\\s+", " ").trim()) {
            case "TITLE" -> "title";
            case "ABSTRACT" -> "abstract";
            case "KEYWORDS" -> "keywords";
            case "INTRODUCTION" -> "introduction";
            case "LITERATURE REVIEW" -> "literatureReview";
            case "METHODOLOGY" -> "methodology";
            case "CONCLUSION" -> "conclusion";
            default -> heading.toLowerCase();
        };
    }
}
