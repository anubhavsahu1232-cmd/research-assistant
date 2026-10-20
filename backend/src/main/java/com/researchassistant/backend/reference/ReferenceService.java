
package com.researchassistant.backend.reference;

import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReferenceService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final File file = new File("data/research-references.json");

    public ReferenceService() {
        try {
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }

            if (!file.exists()) {
                objectMapper.writeValue(file, new ArrayList<>());
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize references", e);
        }
    }

    public synchronized Map<String, Object> addReference(
            Map<String, String> request) {
        try {
            List<Map<String, Object>> references = getAllReferences();

            Map<String, Object> reference = new LinkedHashMap<>();
            reference.put("id", UUID.randomUUID().toString());
            reference.put("paperId", request.get("paperId"));
            reference.put("title", request.get("title"));
            reference.put("authors", request.get("authors"));
            reference.put("year", request.get("year"));
            reference.put("doi", request.get("doi"));
            reference.put("url", request.get("url"));
            reference.put("citation", request.get("citation"));
            reference.put("createdAt", LocalDateTime.now().toString());

            references.add(0, reference);
            objectMapper.writeValue(file, references);

            return reference;
        } catch (Exception e) {
            throw new RuntimeException("Unable to save reference", e);
        }
    }

    public synchronized List<Map<String, Object>> getAllReferences() {
        try {
            if (!file.exists()) {
                return new ArrayList<>();
            }

            return objectMapper.readValue(
                    file,
                    new TypeReference<List<Map<String, Object>>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("Unable to read references", e);
        }
    }

    public synchronized boolean deleteReference(String id) {
        try {
            List<Map<String, Object>> references = getAllReferences();

            boolean removed = references.removeIf(
                    reference -> id.equals(
                            String.valueOf(reference.get("id")))
            );

            if (removed) {
                objectMapper.writeValue(file, references);
            }

            return removed;
        } catch (Exception e) {
            throw new RuntimeException("Unable to delete reference", e);
        }
    }
}
