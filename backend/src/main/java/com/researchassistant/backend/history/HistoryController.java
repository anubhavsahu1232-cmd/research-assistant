
package com.researchassistant.backend.history;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @PostMapping
    public ResponseEntity<?> addHistory(
            @RequestBody Map<String, String> request) {

        String title = request.get("title");
        String paperId = request.get("paperId");
        String url = request.get("url");

        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Title is required"));
        }

        Map<String, Object> saved = historyService.addHistory(
                paperId, title, url);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<Map<String, Object>> getHistory() {
        return historyService.getAllHistory();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHistory(@PathVariable String id) {
        boolean deleted = historyService.deleteHistory(id);

        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "History item not found"));
        }

        return ResponseEntity.ok(
                Map.of("message", "History deleted successfully"));
    }

    @DeleteMapping
    public ResponseEntity<?> clearHistory() {
        historyService.clearHistory();

        return ResponseEntity.ok(
                Map.of("message", "Reading history cleared successfully"));
    }
}
