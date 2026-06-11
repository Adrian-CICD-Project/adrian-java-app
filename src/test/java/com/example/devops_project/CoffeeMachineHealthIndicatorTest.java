package com.example.devops_project;

import com.example.devops_project.chaos.ChaosStateService;
import com.example.devops_project.config.ChaosProperties;
import com.example.devops_project.health.CoffeeMachineHealthIndicator;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;

class CoffeeMachineHealthIndicatorTest {

    private ChaosStateService chaosState;
    private CoffeeMachineHealthIndicator indicator;

    @BeforeEach
    void setUp() {
        chaosState = new ChaosStateService(new ChaosProperties(), new SimpleMeterRegistry());
        indicator = new CoffeeMachineHealthIndicator(chaosState);
    }

    @Test
    void health_ShouldBeUpByDefault() {
        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void health_ShouldBeDownWhenReadinessSabotaged() {
        chaosState.breakReadiness();
        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);

        chaosState.recoverReadiness();
        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
    }
}
