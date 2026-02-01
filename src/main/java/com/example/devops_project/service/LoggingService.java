package com.example.devops_project.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class LoggingService {

    private final Logger logger;

    public LoggingService() {
        this(LoggerFactory.getLogger(LoggingService.class));
    }

    public LoggingService(Logger logger) {
        this.logger = logger;
    }

    public void logMessage() {
        logger.info(generateLogMessage());
    }

    public void init() {
        logger.info("LoggingService uruchomiony");
    }

    public String generateLogMessage() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "Test log: " + LocalDateTime.now().format(formatter);
    }
}
