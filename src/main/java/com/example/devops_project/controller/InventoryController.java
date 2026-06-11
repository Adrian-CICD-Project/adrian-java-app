package com.example.devops_project.controller;

import com.example.devops_project.service.InventoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public Map<String, Integer> getStock() {
        return inventoryService.getStock();
    }

    @PostMapping("/restock")
    public Map<String, Integer> restock() {
        return inventoryService.restock();
    }
}
