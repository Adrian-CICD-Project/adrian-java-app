package com.example.devops_project.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api")
public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello World");
    }

    @GetMapping("/error500")
    public ResponseEntity<String> error500() {
        logger.error("Error 500");
        return ResponseEntity.internalServerError().body("Error 500");
    }

    @GetMapping("/error400")
    public ResponseEntity<String> error400() {
        logger.error("Error 400");
        return ResponseEntity.badRequest().body("Error 400");
    }
}
