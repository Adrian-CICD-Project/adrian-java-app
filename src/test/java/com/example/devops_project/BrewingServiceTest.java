package com.example.devops_project;

import com.example.devops_project.chaos.ChaosStateService;
import com.example.devops_project.config.ChaosProperties;
import com.example.devops_project.exception.CoffeeMachineException;
import com.example.devops_project.model.MenuItem;
import com.example.devops_project.service.BrewingService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrewingServiceTest {

    private static final MenuItem ESPRESSO =
            new MenuItem("espresso", "Espresso", 9.00, Map.of("coffee_beans", 18));

    private ChaosStateService chaosState;
    private BrewingService brewingService;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        ChaosProperties properties = new ChaosProperties();
        properties.setMaxDelayMs(100);
        chaosState = new ChaosStateService(properties, meterRegistry);
        brewingService = new BrewingService(chaosState, properties, meterRegistry);
    }

    @Test
    void brew_MachineUp_ShouldSucceedAndRecordTimer() {
        assertThatCode(() -> brewingService.brew(ESPRESSO)).doesNotThrowAnyException();
        assertThat(meterRegistry.timer("coffee.brew.duration").count()).isEqualTo(1);
    }

    @Test
    void brew_MachineBroken_ShouldThrow() {
        chaosState.breakMachine();
        assertThatThrownBy(() -> brewingService.brew(ESPRESSO))
                .isInstanceOf(CoffeeMachineException.class)
                .hasMessageContaining("Espresso");
    }

    @Test
    void slowBrew_ShouldClampDelayToMax() {
        assertThat(brewingService.slowBrew(5000)).isEqualTo(100);
        assertThat(brewingService.slowBrew(-10)).isZero();
        assertThat(brewingService.slowBrew(50)).isEqualTo(50);
    }
}
