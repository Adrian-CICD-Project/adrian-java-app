package com.example.devops_project.health;

import com.example.devops_project.chaos.ChaosStateService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Wpięty w grupę readiness (application.properties:
 * management.endpoint.health.group.readiness.include=readinessState,coffeeMachine).
 */
@Component("coffeeMachine")
public class CoffeeMachineHealthIndicator implements HealthIndicator {

    private final ChaosStateService chaosState;

    public CoffeeMachineHealthIndicator(ChaosStateService chaosState) {
        this.chaosState = chaosState;
    }

    @Override
    public Health health() {
        if (chaosState.isReadinessBroken()) {
            return Health.down().withDetail("reason", "readiness sabotaged via /api/chaos/health/readiness/fail").build();
        }
        return Health.up().build();
    }
}
