package com.example.devops_project.exception;

public class OutOfStockException extends RuntimeException {

    public OutOfStockException(String ingredient) {
        super("Ingredient out of stock: " + ingredient);
    }
}
