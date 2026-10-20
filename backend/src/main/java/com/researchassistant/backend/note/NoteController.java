
package com.researchassistant.backend.note;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<?> addNote(
            @RequestBody(required = false) JsonNode request) {

        if (request == null || !request.isObject()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "A valid JSON request is required"));
        }

        String title = request.path("title").asText("").trim();
        String content = request.path("content").asText("").trim();
        String paperId = request.path("paperId").asText("").trim();

        if (title.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Note title is required"));
        }

        if (title.length() > 300) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Title must not exceed 300 characters"));
        }

        if (content.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Note content is required"));
        }

        if (content.length() > 10000) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Content must not exceed 10000 characters"));
        }

        if (paperId.length() > 100) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Paper ID must not exceed 100 characters"));
        }

        try {
            JsonNode note = noteService.addNote(title, content, paperId);

            return ResponseEntity.status(HttpStatus.CREATED).body(note);

        } catch (IOException e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Could not save note"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllNotes() {
        try {
            return ResponseEntity.ok(noteService.getAllNotes());

        } catch (IOException e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Could not read notes"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable String id) {
        if (id.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Note ID is required"));
        }

        try {
            boolean deleted = noteService.deleteNote(id);

            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Note not found"));
            }

            return ResponseEntity.ok(
                    Map.of("message", "Note deleted successfully"));

        } catch (IOException e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Could not delete note"));
        }
    }
}
