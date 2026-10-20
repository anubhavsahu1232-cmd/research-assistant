
package com.researchassistant.backend.bookmark;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping
    public ResponseEntity<?> addBookmark(
            @RequestBody(required = false) JsonNode request) {

        if (request == null || !request.isObject()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "A valid JSON request is required"));
        }

        String title = request.path("title").asText("").trim();
        String url = request.path("url").asText("").trim();
        String note = request.path("note").asText("").trim();

        if (title.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Bookmark title is required"));
        }

        if (title.length() > 500) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Title must not exceed 500 characters"));
        }

        if (url.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Bookmark URL is required"));
        }

        if (url.length() > 2048) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "URL must not exceed 2048 characters"));
        }

        if (note.length() > 2000) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Note must not exceed 2000 characters"));
        }

        try {
            JsonNode bookmark =
                    bookmarkService.addBookmark(title, url, note);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(bookmark);

        } catch (IOException e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Could not save bookmark"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllBookmarks() {
        try {
            return ResponseEntity.ok(
                    bookmarkService.getAllBookmarks());

        } catch (IOException e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Could not read bookmarks"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBookmark(
            @PathVariable String id) {

        if (id.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Bookmark ID is required"));
        }

        try {
            boolean deleted = bookmarkService.deleteBookmark(id);

            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Bookmark not found"));
            }

            return ResponseEntity.ok(
                    Map.of("message", "Bookmark deleted successfully"));

        } catch (IOException e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Could not delete bookmark"));
        }
    }
}
