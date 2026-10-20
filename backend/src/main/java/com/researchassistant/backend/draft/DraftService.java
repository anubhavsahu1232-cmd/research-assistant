
package com.researchassistant.backend.draft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Service
public class DraftService {

    private final ObjectMapper objectMapper;

    private final Path storagePath =
            Path.of("data", "research-drafts.json");

    public DraftService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private ArrayNode readDrafts() throws IOException {

        if (Files.notExists(storagePath)) {
            return objectMapper.createArrayNode();
        }

        JsonNode data = objectMapper.readTree(storagePath.toFile());

        if (data == null || !data.isArray()) {
            throw new IOException(
                    "Draft storage file contains invalid JSON data."
            );
        }

        return (ArrayNode) data;
    }

    private void writeDrafts(ArrayNode drafts) throws IOException {

        Files.createDirectories(storagePath.getParent());

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(storagePath.toFile(), drafts);
    }

    public synchronized JsonNode saveDraft(
            String topic,
            String title,
            JsonNode paper) throws IOException {

        ArrayNode drafts = readDrafts();

        ObjectNode draft = objectMapper.createObjectNode();

        draft.put("id", UUID.randomUUID().toString());
        draft.put("topic", topic);
        draft.put("title", title);
        draft.put("createdAt", Instant.now().toString());
        draft.set("paper", paper.deepCopy());

        drafts.add(draft);
        writeDrafts(drafts);

        return draft;
    }

    public synchronized JsonNode updateDraft(
            String id,
            String topic,
            String title,
            JsonNode paper) throws IOException {

        ArrayNode drafts = readDrafts();

        for (int i = 0; i < drafts.size(); i++) {

            JsonNode existing = drafts.get(i);

            if (existing.path("id").asText().equals(id)) {

                ObjectNode updatedDraft =
                        ((ObjectNode) existing).deepCopy();

                updatedDraft.put("topic", topic);
                updatedDraft.put("title", title);
                updatedDraft.put("updatedAt", Instant.now().toString());
                updatedDraft.set("paper", paper.deepCopy());

                drafts.set(i, updatedDraft);
                writeDrafts(drafts);

                return updatedDraft;
            }
        }

        return null;
    }

    public synchronized JsonNode getAllDrafts() throws IOException {
        return readDrafts();
    }

    public synchronized JsonNode getDraftById(
            String id) throws IOException {

        ArrayNode drafts = readDrafts();

        for (JsonNode draft : drafts) {
            if (draft.path("id").asText().equals(id)) {
                return draft;
            }
        }

        return null;
    }
}
