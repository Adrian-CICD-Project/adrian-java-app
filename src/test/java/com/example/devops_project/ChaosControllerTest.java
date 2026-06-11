package com.example.devops_project;

import com.example.devops_project.chaos.ChaosStateService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "coffee.chaos.max-delay-ms=200",
        "coffee.chaos.max-memory-mb=3"
})
@AutoConfigureMockMvc
class ChaosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChaosStateService chaosState;

    @AfterEach
    void cleanUp() {
        chaosState.repairMachine();
        chaosState.recoverReadiness();
        chaosState.releaseMemory();
    }

    @Test
    void slowBrew_ShouldClampDelayToConfiguredMax() throws Exception {
        mockMvc.perform(get("/api/chaos/slow-brew").param("delayMs", "99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.delayMs").value(200));
    }

    @Test
    void machineBreakAndRepair_ShouldToggleState() throws Exception {
        mockMvc.perform(post("/api/chaos/machine/break"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.machineBroken").value(true));

        mockMvc.perform(get("/api/chaos/status"))
                .andExpect(jsonPath("$.machineBroken").value(true));

        mockMvc.perform(post("/api/chaos/machine/repair"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.machineBroken").value(false));
    }

    @Test
    void readinessSabotage_ShouldFlipActuatorReadinessProbe() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/chaos/health/readiness/fail"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isServiceUnavailable());

        mockMvc.perform(post("/api/chaos/health/readiness/recover"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk());
    }

    @Test
    void livenessSabotage_ShouldFlipActuatorLivenessProbe() throws Exception {
        mockMvc.perform(post("/api/chaos/health/liveness/fail"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isServiceUnavailable());

        mockMvc.perform(post("/api/chaos/health/liveness/recover"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk());
    }

    @Test
    void memoryAllocation_ShouldRespectCapAndRelease() throws Exception {
        mockMvc.perform(post("/api/chaos/memory").param("mb", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retainedMemoryMb").value(2));

        // cap to 3 MB — kolejna alokacja 2 MB zostaje przycięta do 1 MB
        mockMvc.perform(post("/api/chaos/memory").param("mb", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retainedMemoryMb").value(3));

        mockMvc.perform(post("/api/chaos/memory/release"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retainedMemoryMb").value(0));
    }

    @Test
    void cpuBurn_ShouldStartBoundedBackgroundWork() throws Exception {
        mockMvc.perform(post("/api/chaos/cpu").param("seconds", "1").param("threads", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.burnSeconds").value(1));
    }

    @Test
    void status_ShouldReportAllToggles() throws Exception {
        mockMvc.perform(get("/api/chaos/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chaosEnabled").value(true))
                .andExpect(jsonPath("$.readinessBroken").value(false));
    }
}
