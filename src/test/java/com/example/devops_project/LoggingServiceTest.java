package com.example.devops_project;

import com.example.devops_project.service.LoggingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LoggingServiceTest {

    private LoggingService loggingService;
    private Logger loggerMock;

    @BeforeEach
    void setUp() {

        loggerMock = mock(Logger.class);

        loggingService = new LoggingService(loggerMock);
    }

    @Test
    void generateLogMessage_ShouldReturnCorrectFormat() {
        String logMessage = loggingService.generateLogMessage();
        assertThat(logMessage).startsWith("Test log: ");

        String dateTimePart = logMessage.substring("Test log: ".length());
        assertThat(dateTimePart).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    void logMessage_ShouldLogCorrectly() {
        loggingService.logMessage();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(loggerMock, times(1)).info(captor.capture());

        String loggedMessage = captor.getValue();
        assertThat(loggedMessage).startsWith("Test log: ");
    }

    @Test
    void init_ShouldLogInitializationMessage() {
        loggingService.init();

        verify(loggerMock, times(1)).info("LoggingService uruchomiony");
    }

    @Test
    void logMessage_ShouldHandleExceptionsGracefully() {
        doThrow(new RuntimeException("Simulated exception")).when(loggerMock).info(anyString());

        try {
            loggingService.logMessage();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class)
                    .hasMessage("Simulated exception");
        }
    }
}
