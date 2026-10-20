
package com.researchassistant.backend.reference;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/references")
public class ReferenceController {

    private final ReferenceService referenceService;

    public ReferenceController(ReferenceService referenceService) {
        this.referenceService = referenceService;
    }

    // Add a new reference
    @PostMapping
    public ResponseEntity<?> addReference(
            @RequestBody Map<String, String> request) {

        String title = request.get("title");

        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Reference title is required"));
        }

        Map<String, Object> saved =
                referenceService.addReference(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Get all references
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getReferences() {
        return ResponseEntity.ok(
                referenceService.getAllReferences()
        );
    }

    // Delete a reference by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReference(@PathVariable String id) {

        boolean deleted = referenceService.deleteReference(id);

        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Reference not found"));
        }

        return ResponseEntity.ok(
                Map.of("message", "Reference deleted successfully")
        );
    }
}
