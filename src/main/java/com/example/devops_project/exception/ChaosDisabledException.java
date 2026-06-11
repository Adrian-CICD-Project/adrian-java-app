package com.example.devops_project.exception;

public class ChaosDisabledException extends RuntimeException {

    public ChaosDisabledException() {
        super("Chaos endpoints are disabled (coffee.chaos.enabled=false)");
    }
}
