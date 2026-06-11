package com.example.devops_project;

import com.example.devops_project.chaos.ChaosStateService;
import com.example.devops_project.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ChaosStateService chaosState;

    @BeforeEach
    void resetState() {
        inventoryService.restock();
        chaosState.repairMachine();
    }

    @Test
    void getStock_ShouldReturnAllIngredients() throws Exception {
        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coffee_beans").value(360))
                .andExpect(jsonPath("$.milk").value(1500));
    }

    @Test
    void restock_ShouldResetLevels() throws Exception {
        mockMvc.perform(post("/api/orders").param("itemId", "latte"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/inventory"))
                .andExpect(jsonPath("$.coffee_beans").value(342));

        mockMvc.perform(post("/api/inventory/restock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coffee_beans").value(360));
    }
}
