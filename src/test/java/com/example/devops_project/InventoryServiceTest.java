package com.example.devops_project;

import com.example.devops_project.exception.OutOfStockException;
import com.example.devops_project.service.InventoryService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryServiceTest {

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(new SimpleMeterRegistry());
    }

    @Test
    void consume_ShouldDecrementStock() {
        inventoryService.consume(Map.of("coffee_beans", 18, "water", 30));

        assertThat(inventoryService.getStock().get("coffee_beans")).isEqualTo(342);
        assertThat(inventoryService.getStock().get("water")).isEqualTo(1970);
    }

    @Test
    void consume_InsufficientStock_ShouldThrowWithoutPartialConsumption() {
        assertThatThrownBy(() -> inventoryService.consume(Map.of("coffee_beans", 18, "chocolate", 999)))
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining("chocolate");

        // nic nie zostało zużyte — walidacja przed dekrementacją
        assertThat(inventoryService.getStock().get("coffee_beans")).isEqualTo(360);
    }

    @Test
    void consume_UnknownIngredient_ShouldThrow() {
        assertThatThrownBy(() -> inventoryService.consume(Map.of("tea_leaves", 1)))
                .isInstanceOf(OutOfStockException.class);
    }

    @Test
    void restock_ShouldResetToDefaults() {
        inventoryService.consume(Map.of("milk", 500));
        inventoryService.restock();

        assertThat(inventoryService.getStock().get("milk")).isEqualTo(1500);
    }
}
