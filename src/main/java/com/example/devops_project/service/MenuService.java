package com.example.devops_project.service;

import com.example.devops_project.exception.MenuItemNotFoundException;
import com.example.devops_project.model.MenuItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MenuService {

    private static final List<MenuItem> MENU = List.of(
            new MenuItem("espresso", "Espresso", 9.00, Map.of("coffee_beans", 18, "water", 30)),
            new MenuItem("americano", "Americano", 10.00, Map.of("coffee_beans", 18, "water", 120)),
            new MenuItem("latte", "Caffe Latte", 14.00, Map.of("coffee_beans", 18, "water", 30, "milk", 200)),
            new MenuItem("cappuccino", "Cappuccino", 13.00, Map.of("coffee_beans", 18, "water", 30, "milk", 120)),
            new MenuItem("flat-white", "Flat White", 15.00, Map.of("coffee_beans", 36, "water", 60, "milk", 130)),
            new MenuItem("mocha", "Caffe Mocha", 16.00, Map.of("coffee_beans", 18, "water", 30, "milk", 150, "chocolate", 20))
    );

    public List<MenuItem> getMenu() {
        return MENU;
    }

    public MenuItem getItem(String id) {
        return MENU.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new MenuItemNotFoundException(id));
    }
}
