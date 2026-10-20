
package com.researchassistant.backend.note;

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
public class NoteService {

    private final ObjectMapper objectMapper;

    private final Path storagePath =
            Path.of("data", "research-notes.json");

    public NoteService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private ArrayNode readNotes() throws IOException {
        if (Files.notExists(storagePath)) {
            return objectMapper.createArrayNode();
        }

        JsonNode data = objectMapper.readTree(storagePath.toFile());

        if (data == null || !data.isArray()) {
            throw new IOException("Invalid note storage data.");
        }

        return (ArrayNode) data;
    }

    private void writeNotes(ArrayNode notes) throws IOException {
        Files.createDirectories(storagePath.getParent());

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(storagePath.toFile(), notes);
    }

    public synchronized JsonNode addNote(
            String title, String content, String paperId)
            throws IOException {

        ArrayNode notes = readNotes();
        ObjectNode note = objectMapper.createObjectNode();

        note.put("id", UUID.randomUUID().toString());
        note.put("title", title);
        note.put("content", content);
        note.put("paperId", paperId);
        note.put("createdAt", Instant.now().toString());

        notes.add(note);
        writeNotes(notes);

        return note;
    }

    public synchronized JsonNode getAllNotes() throws IOException {
        return readNotes();
    }

    public synchronized boolean deleteNote(String id)
            throws IOException {

        ArrayNode notes = readNotes();

        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).path("id").asText().equals(id)) {
                notes.remove(i);
                writeNotes(notes);
                return true;
            }
        }

        return false;
    }
}
