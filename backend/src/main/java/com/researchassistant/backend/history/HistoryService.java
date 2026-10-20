
package com.researchassistant.backend.history;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class HistoryService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final File file = new File("data/research-history.json");

    public HistoryService() {
        try {
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }

            if (!file.exists()) {
                objectMapper.writeValue(file, new ArrayList<>());
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize reading history", e);
        }
    }

    public synchronized Map<String, Object> addHistory(
            String paperId, String title, String url) {
        try {
            List<Map<String, Object>> history = getAllHistory();

            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("id", UUID.randomUUID().toString());
            item.put("paperId", paperId);
            item.put("title", title);
            item.put("url", url);
            item.put("readAt", LocalDateTime.now().toString());

            history.add(0, item);
            objectMapper.writeValue(file, history);

            return item;
        } catch (Exception e) {
            throw new RuntimeException("Unable to save reading history", e);
        }
    }

    public synchronized List<Map<String, Object>> getAllHistory() {
        try {
            if (!file.exists()) {
                return new ArrayList<>();
            }

            return objectMapper.readValue(
                    file,
                    new TypeReference<List<Map<String, Object>>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("Unable to read history", e);
        }
    }

    public synchronized boolean deleteHistory(String id) {
        try {
            List<Map<String, Object>> history = getAllHistory();

            boolean removed = history.removeIf(
                    item -> id.equals(String.valueOf(item.get("id")))
            );

            if (removed) {
                objectMapper.writeValue(file, history);
            }

            return removed;
        } catch (Exception e) {
            throw new RuntimeException("Unable to delete history", e);
        }
    }

    public synchronized void clearHistory() {
        try {
            objectMapper.writeValue(file, new ArrayList<>());
        } catch (Exception e) {
            throw new RuntimeException("Unable to clear history", e);
        }
    }
}
