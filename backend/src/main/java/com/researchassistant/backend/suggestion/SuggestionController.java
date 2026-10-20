
package com.researchassistant.backend.suggestion;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    @PostMapping
    public ResponseEntity<?> suggest(
            @RequestBody Map<String, String> request) {

        String topic = request.get("topic");

        if (topic == null || topic.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Research topic is required"));
        }

        topic = topic.trim();

        List<String> topics = List.of(
                "Recent advancements in " + topic,
                "Applications of " + topic + " in real-world problems",
                "Challenges and limitations of " + topic,
                "Future research directions in " + topic,
                "Comparative analysis of methods used in " + topic
        );

        List<String> queries = List.of(
                "\"" + topic + "\" research papers",
                "\"" + topic + "\" systematic literature review",
                "\"" + topic + "\" recent advances 2024 2025 2026",
                "\"" + topic + "\" challenges and future directions",
                "\"" + topic + "\" methodology comparative study"
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("originalTopic", topic);
        response.put("suggestedTopics", topics);
        response.put("searchQueries", queries);

        return ResponseEntity.ok(response);
    }
}
