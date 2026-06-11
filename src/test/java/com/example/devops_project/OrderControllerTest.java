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
class OrderControllerTest {

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
    void placeOrder_ShouldReturnCreatedOrder() throws Exception {
        mockMvc.perform(post("/api/orders").param("itemId", "latte"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemId").value("latte"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void placeOrder_UnknownItem_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(post("/api/orders").param("itemId", "tea"))
                .andExpect(status().isNotFound());
    }

    @Test
    void placeOrder_OutOfStock_ShouldReturnConflict() throws Exception {
        // mocha zużywa 20 czekolady, stan początkowy to 100 — szóste zamówienie odpada
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/orders").param("itemId", "mocha"))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/api/orders").param("itemId", "mocha"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Ingredient out of stock: chocolate"));
    }

    @Test
    void placeOrder_MachineBroken_ShouldReturnInternalServerError() throws Exception {
        chaosState.breakMachine();
        mockMvc.perform(post("/api/orders").param("itemId", "espresso"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("Coffee machine is broken - cannot brew Espresso"));
    }

    @Test
    void getOrders_ShouldListPlacedOrders() throws Exception {
        mockMvc.perform(post("/api/orders").param("itemId", "espresso"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.itemId == 'espresso')]").exists());
    }
}
