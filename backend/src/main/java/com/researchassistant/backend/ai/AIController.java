
package com.researchassistant.backend.ai;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    // Generate complete research paper
    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generate(
            @RequestBody(required = false)
            Map<String, String> request) {

        if (request == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Request body is required")
            );
        }

        String topic = request.get("topic");

        if (topic == null || topic.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Research topic is required")
            );
        }

        topic = topic.trim();

        if (topic.length() < 3) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "error",
                            "Research topic must be at least 3 characters"
                    )
            );
        }

        if (topic.length() > 300) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "error",
                            "Research topic must not exceed 300 characters"
                    )
            );
        }

        try {
            Map<String, String> paper =
                    aiService.generateResearchPaper(topic);

            return ResponseEntity.ok(paper);

        } catch (Exception e) {
            return ResponseEntity.status(
                    HttpStatus.SERVICE_UNAVAILABLE
            ).body(
                    Map.of(
                            "error",
                            "Paper generation failed. Check whether Ollama is running."
                    )
            );
        }
    }

    // Improve an existing paper section
    @PostMapping("/improve-section")
    public ResponseEntity<Map<String, String>> improveSection(
            @RequestBody(required = false)
            Map<String, String> request) {

        if (request == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Request body is required")
            );
        }

        String topic = request.get("topic");
        String section = request.get("section");
        String content = request.get("content");
        String instruction = request.get("instruction");

        if (topic == null || topic.isBlank()
                || section == null || section.isBlank()
                || content == null || content.isBlank()
                || instruction == null || instruction.isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "error",
                            "Topic, section, content and instruction are required"
                    )
            );
        }

        topic = topic.trim();
        section = section.trim();
        content = content.trim();
        instruction = instruction.trim();

        if (topic.length() > 300) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Topic must not exceed 300 characters")
            );
        }

        if (content.length() > 15000) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "error",
                            "Section content must not exceed 15000 characters"
                    )
            );
        }

        if (instruction.length() > 1000) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "error",
                            "Instruction must not exceed 1000 characters"
                    )
            );
        }

        try {
            String improved = aiService.improveSection(
                    topic,
                    section,
                    content,
                    instruction
            );

            return ResponseEntity.ok(
                    Map.of(
                            "topic", topic,
                            "section", section,
                            "improvedContent", improved
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(
                    HttpStatus.SERVICE_UNAVAILABLE
            ).body(
                    Map.of(
                            "error",
                            "Section improvement failed. Check whether Ollama is running."
                    )
            );
        }
    }

    // Generate an abstract and keywords for a research topic
    @PostMapping("/generate-abstract")
    public ResponseEntity<Map<String, String>> generateAbstract(
            @RequestBody(required = false)
            Map<String, String> request) {

        if (request == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Request body is required")
            );
        }

        String topic = request.get("topic");

        if (topic == null || topic.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Research topic is required")
            );
        }

        topic = topic.trim();

        if (topic.length() < 3 || topic.length() > 300) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "error",
                            "Topic must be between 3 and 300 characters"
                    )
            );
        }

        try {
            Map<String, String> result =
                    aiService.generateAbstractAndKeywords(topic);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(
                    HttpStatus.SERVICE_UNAVAILABLE
            ).body(
                    Map.of(
                            "error",
                            "Abstract generation failed. Check whether Ollama is running."
                    )
            );
        }
    }
}
