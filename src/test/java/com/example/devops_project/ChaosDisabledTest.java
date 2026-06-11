package com.example.devops_project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "coffee.chaos.enabled=false")
@AutoConfigureMockMvc
class ChaosDisabledTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void chaosEndpoints_ShouldReturnForbiddenWhenDisabled() throws Exception {
        mockMvc.perform(post("/api/chaos/machine/break"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(get("/api/chaos/status"))
                .andExpect(status().isForbidden());
    }

    @Test
    void businessEndpoints_ShouldStillWorkWhenChaosDisabled() throws Exception {
        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk());
    }
}
