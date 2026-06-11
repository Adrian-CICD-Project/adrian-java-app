package com.example.devops_project.model;

import java.time.Instant;

public record Order(long id, String itemId, OrderStatus status, Instant createdAt) {
}
