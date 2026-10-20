
package com.researchassistant.backend.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() throws IOException {
        return adminService.getDashboard();
    }

    @GetMapping("/drafts")
    public JsonNode getDrafts() throws IOException {
        return adminService.getAllDrafts();
    }

    @GetMapping("/bookmarks")
    public JsonNode getBookmarks() throws IOException {
        return adminService.getAllBookmarks();
    }

    @GetMapping("/notes")
    public JsonNode getNotes() throws IOException {
        return adminService.getAllNotes();
    }

    @GetMapping("/history")
    public JsonNode getHistory() throws IOException {
        return adminService.getAllHistory();
    }

    @GetMapping("/references")
    public JsonNode getReferences() throws IOException {
        return adminService.getAllReferences();
    }
}
