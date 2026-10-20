
package com.researchassistant.backend.admin;

import com.researchassistant.backend.draft.DraftService;
import com.researchassistant.backend.bookmark.BookmarkService;
import com.researchassistant.backend.note.NoteService;

import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdminService {

    private final DraftService draftService;
    private final BookmarkService bookmarkService;
    private final NoteService noteService;
    private final ObjectMapper objectMapper;

    public AdminService(
            DraftService draftService,
            BookmarkService bookmarkService,
            NoteService noteService,
            ObjectMapper objectMapper) {

        this.draftService = draftService;
        this.bookmarkService = bookmarkService;
        this.noteService = noteService;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getDashboard() throws IOException {

        Map<String, Object> dashboard = new LinkedHashMap<>();

        dashboard.put("totalDrafts",
                draftService.getAllDrafts().size());

        dashboard.put("totalBookmarks",
                bookmarkService.getAllBookmarks().size());

        dashboard.put("totalNotes",
                noteService.getAllNotes().size());

        dashboard.put("totalHistory",
                countRecords("data/research-history.json"));

        dashboard.put("totalReferences",
                countRecords("data/research-references.json"));

        dashboard.put("status", "success");

        return dashboard;
    }

    public JsonNode getAllDrafts() throws IOException {
        return draftService.getAllDrafts();
    }

    public JsonNode getAllBookmarks() throws IOException {
        return bookmarkService.getAllBookmarks();
    }

    public JsonNode getAllNotes() throws IOException {
        return noteService.getAllNotes();
    }

    public JsonNode getAllHistory() throws IOException {
        return readArrayFile("data/research-history.json");
    }

    public JsonNode getAllReferences() throws IOException {
        return readArrayFile("data/research-references.json");
    }

    private int countRecords(String filePath) throws IOException {
        return readArrayFile(filePath).size();
    }

    private JsonNode readArrayFile(String filePath) throws IOException {

        Path path = Path.of(filePath);

        if (Files.notExists(path)) {
            return objectMapper.createArrayNode();
        }

        JsonNode data = objectMapper.readTree(path.toFile());

        if (data == null || !data.isArray()) {
            throw new IOException(
                    "Invalid JSON array in " + filePath);
        }

        return data;
    }
}
