package com.example.devops_project.controller;

import com.example.devops_project.model.MenuItem;
import com.example.devops_project.service.MenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<MenuItem> getMenu() {
        return menuService.getMenu();
    }

    @GetMapping("/{id}")
    public MenuItem getItem(@PathVariable String id) {
        return menuService.getItem(id);
    }
}
