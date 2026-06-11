package com.example.devops_project.service;

import com.example.devops_project.chaos.ChaosStateService;
import com.example.devops_project.config.ChaosProperties;
import com.example.devops_project.exception.CoffeeMachineException;
import com.example.devops_project.model.MenuItem;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class BrewingService {

    private final ChaosStateService chaosState;
    private final ChaosProperties chaosProperties;
    private final Timer brewTimer;

    public BrewingService(ChaosStateService chaosState, ChaosProperties chaosProperties, MeterRegistry meterRegistry) {
        this.chaosState = chaosState;
        this.chaosProperties = chaosProperties;
        this.brewTimer = Timer.builder("coffee.brew.duration")
                .description("Time spent brewing coffee")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    public void brew(MenuItem item) {
        brewTimer.record(() -> {
            if (chaosState.isMachineBroken()) {
                throw new CoffeeMachineException("Coffee machine is broken - cannot brew " + item.name());
            }
        });
    }

    /** Parzenie z wymuszonym opóźnieniem — zasila histogramy p95/p99. Zwraca faktyczne opóźnienie. */
    public long slowBrew(long delayMs) {
        long actualDelay = Math.clamp(delayMs, 0, chaosProperties.getMaxDelayMs());
        brewTimer.record(() -> {
            try {
                Thread.sleep(actualDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CoffeeMachineException("Brewing interrupted");
            }
        });
        return actualDelay;
    }
}
