package com.example.wojtek_java_maven.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello My firend!";
    }

    @GetMapping("/goodbye")
    public String sayGoodbye() {
        return "Goodbye my friend!";
    }
}
