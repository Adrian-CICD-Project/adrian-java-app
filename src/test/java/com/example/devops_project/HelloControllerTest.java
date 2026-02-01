package com.example.devops_project;

import com.example.devops_project.controller.HelloController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloController.class)
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void hello_ShouldReturnHelloWorld() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World"))
                .andExpect(header().string("Content-Type", MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"));
    }

    @Test
    void error500_ShouldReturnInternalServerErrorWithMessage() throws Exception {
        mockMvc.perform(get("/api/error500"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error 500"));
    }

    @Test
    void error400_ShouldReturnBadRequestWithMessage() throws Exception {
        mockMvc.perform(get("/api/error400"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error 400"));
    }

    @Test
    void invalidEndpoint_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/invalid"))
                .andExpect(status().isNotFound());
    }
}
