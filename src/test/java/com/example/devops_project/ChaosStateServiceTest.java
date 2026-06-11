package com.example.devops_project;

import com.example.devops_project.chaos.ChaosStateService;
import com.example.devops_project.config.ChaosProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChaosStateServiceTest {

    private ChaosProperties properties;
    private ChaosStateService chaosState;

    @BeforeEach
    void setUp() {
        properties = new ChaosProperties();
        properties.setMaxMemoryMb(3);
        properties.setMaxCpuSeconds(2);
        properties.setMaxCpuThreads(2);
        chaosState = new ChaosStateService(properties, new SimpleMeterRegistry());
    }

    @Test
    void machineToggle_ShouldFlipState() {
        assertThat(chaosState.isMachineBroken()).isFalse();
        chaosState.breakMachine();
        assertThat(chaosState.isMachineBroken()).isTrue();
        chaosState.repairMachine();
        assertThat(chaosState.isMachineBroken()).isFalse();
    }

    @Test
    void readinessToggle_ShouldFlipState() {
        chaosState.breakReadiness();
        assertThat(chaosState.isReadinessBroken()).isTrue();
        chaosState.recoverReadiness();
        assertThat(chaosState.isReadinessBroken()).isFalse();
    }

    @Test
    void allocateMemory_ShouldEnforceCumulativeCap() {
        assertThat(chaosState.allocateMemory(2)).isEqualTo(2);
        // cap 3 MB — druga alokacja przycięta do 1 MB
        assertThat(chaosState.allocateMemory(2)).isEqualTo(3);
        // cap osiągnięty — kolejna alokacja odrzucona
        assertThat(chaosState.allocateMemory(1)).isEqualTo(3);
    }

    @Test
    void allocateMemory_InvalidArgument_ShouldThrow() {
        assertThatThrownBy(() -> chaosState.allocateMemory(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void releaseMemory_ShouldResetRetainedCounter() {
        chaosState.allocateMemory(2);
        chaosState.releaseMemory();
        assertThat(chaosState.getRetainedMemoryMb()).isZero();
    }

    @Test
    void startCpuBurn_ShouldClampSecondsAndThreads() {
        int burnSeconds = chaosState.startCpuBurn(999, 999);
        assertThat(burnSeconds).isEqualTo(2);
    }

    @Test
    void status_ShouldExposeAllToggles() {
        chaosState.breakMachine();
        assertThat(chaosState.status())
                .containsEntry("machineBroken", true)
                .containsEntry("readinessBroken", false)
                .containsEntry("retainedMemoryMb", 0)
                .containsKey("chaosEnabled");
    }
}
