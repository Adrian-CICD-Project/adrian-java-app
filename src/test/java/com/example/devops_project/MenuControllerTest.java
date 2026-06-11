package com.example.devops_project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMenu_ShouldReturnAllItems() throws Exception {
        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))));
    }

    @Test
    void getItem_ShouldReturnSingleItem() throws Exception {
        mockMvc.perform(get("/api/menu/latte"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("latte"))
                .andExpect(jsonPath("$.name").value("Caffe Latte"));
    }

    @Test
    void getItem_UnknownId_ShouldReturnNotFoundProblemDetail() throws Exception {
        mockMvc.perform(get("/api/menu/tea"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Menu item not found: tea"));
    }
}
