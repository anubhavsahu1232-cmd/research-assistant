
package com.researchassistant.backend.draft;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/drafts")
public class DraftController {

    private final DraftService draftService;

    public DraftController(DraftService draftService) {
        this.draftService = draftService;
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveDraft(
            @RequestBody(required = false) JsonNode request) {

        if (request == null || !request.isObject()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "A valid JSON request is required"));
        }

        String topic = request.path("topic").asText("").trim();
        String title = request.path("title").asText("").trim();
        JsonNode paper = request.get("paper");

        ResponseEntity<?> validationError =
                validateDraft(topic, title, paper);

        if (validationError != null) {
            return validationError;
        }

        try {
            JsonNode savedDraft =
                    draftService.saveDraft(topic, title, paper);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(savedDraft);

        } catch (IOException e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Could not save the draft to local storage"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDraft(
            @PathVariable String id,
            @RequestBody(required = false) JsonNode request) {

        if (id == null || id.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Draft ID is required"));
        }

        if (request == null || !request.isObject()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "A valid JSON request is required"));
        }

        String topic = request.path("topic").asText("").trim();
        String title = request.path("title").asText("").trim();
        JsonNode paper = request.get("paper");

        ResponseEntity<?> validationError =
                validateDraft(topic, title, paper);

        if (validationError != null) {
            return validationError;
        }

        try {
            JsonNode updatedDraft = draftService.updateDraft(
                    id, topic, title, paper);

            if (updatedDraft == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Draft not found"));
            }

            return ResponseEntity.ok(updatedDraft);

        } catch (IOException e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Could not update the draft"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllDrafts() {
        try {
            return ResponseEntity.ok(draftService.getAllDrafts());

        } catch (IOException e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Could not read saved drafts"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDraftById(@PathVariable String id) {
        try {
            JsonNode draft = draftService.getDraftById(id);

            if (draft == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Draft not found"));
            }

            return ResponseEntity.ok(draft);

        } catch (IOException e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Could not read the requested draft"));
        }
    }

    private ResponseEntity<?> validateDraft(
            String topic, String title, JsonNode paper) {

        if (topic.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Research topic is required"));
        }

        if (topic.length() > 300) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Topic must not exceed 300 characters"));
        }

        if (title.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Paper title is required"));
        }

        if (title.length() > 500) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Title must not exceed 500 characters"));
        }

        if (paper == null || !paper.isObject() || paper.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error",
                            "Paper content must be a non-empty JSON object"));
        }

        return null;
    }
}
