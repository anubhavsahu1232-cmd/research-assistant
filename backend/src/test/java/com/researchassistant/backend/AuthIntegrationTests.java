package com.researchassistant.backend;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc class AuthIntegrationTests {
 @Autowired MockMvc mvc;
 @Test void registrationAndUnauthorizedWrite() throws Exception {
  mvc.perform(post("/api/papers").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
  mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"tester\",\"email\":\"tester@example.com\",\"password\":\"secret123\"}")).andExpect(status().isCreated()).andExpect(jsonPath("$.token").exists());
  mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"tester@example.com\",\"password\":\"secret123\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.token").exists());
 }
}
