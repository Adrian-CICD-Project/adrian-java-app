package com.example.devops_project.model;

import java.util.Map;

public record MenuItem(String id, String name, double price, Map<String, Integer> ingredients) {
}
