package com.example.devops_project.exception;

public class MenuItemNotFoundException extends RuntimeException {

    public MenuItemNotFoundException(String itemId) {
        super("Menu item not found: " + itemId);
    }
}
