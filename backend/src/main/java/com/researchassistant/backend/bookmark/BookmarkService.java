
package com.researchassistant.backend.bookmark;

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
public class BookmarkService {

    private final ObjectMapper objectMapper;

    private final Path storagePath =
            Path.of("data", "research-bookmarks.json");

    public BookmarkService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private ArrayNode readBookmarks() throws IOException {

        if (Files.notExists(storagePath)) {
            return objectMapper.createArrayNode();
        }

        JsonNode data = objectMapper.readTree(storagePath.toFile());

        if (data == null || !data.isArray()) {
            throw new IOException("Invalid bookmark storage data.");
        }

        return (ArrayNode) data;
    }

    private void writeBookmarks(ArrayNode bookmarks)
            throws IOException {

        Files.createDirectories(storagePath.getParent());

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(storagePath.toFile(), bookmarks);
    }

    public synchronized JsonNode addBookmark(
            String title, String url, String note)
            throws IOException {

        ArrayNode bookmarks = readBookmarks();

        ObjectNode bookmark = objectMapper.createObjectNode();

        bookmark.put("id", UUID.randomUUID().toString());
        bookmark.put("title", title);
        bookmark.put("url", url);
        bookmark.put("note", note);
        bookmark.put("createdAt", Instant.now().toString());

        bookmarks.add(bookmark);
        writeBookmarks(bookmarks);

        return bookmark;
    }

    public synchronized JsonNode getAllBookmarks()
            throws IOException {
        return readBookmarks();
    }

    public synchronized boolean deleteBookmark(String id)
            throws IOException {

        ArrayNode bookmarks = readBookmarks();

        for (int i = 0; i < bookmarks.size(); i++) {

            if (bookmarks.get(i).path("id").asText().equals(id)) {
                bookmarks.remove(i);
                writeBookmarks(bookmarks);
                return true;
            }
        }

        return false;
    }
}
