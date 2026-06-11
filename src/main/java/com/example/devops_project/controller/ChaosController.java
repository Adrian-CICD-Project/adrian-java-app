package com.example.devops_project.controller;

import com.example.devops_project.chaos.ChaosStateService;
import com.example.devops_project.config.ChaosProperties;
import com.example.devops_project.exception.ChaosDisabledException;
import com.example.devops_project.service.BrewingService;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpointy chaos engineering — celowe wyzwalanie awarii do demonstracji monitoringu.
 * Wszystkie ograniczone limitami z {@link ChaosProperties} i globalnym włącznikiem.
 */
@RestController
@RequestMapping("/api/chaos")
public class ChaosController {

    private final ChaosStateService chaosState;
    private final ChaosProperties properties;
    private final BrewingService brewingService;
    private final ApplicationEventPublisher eventPublisher;

    public ChaosController(ChaosStateService chaosState, ChaosProperties properties,
                           BrewingService brewingService, ApplicationEventPublisher eventPublisher) {
        this.chaosState = chaosState;
        this.properties = properties;
        this.brewingService = brewingService;
        this.eventPublisher = eventPublisher;
    }

    @ModelAttribute
    public void requireChaosEnabled() {
        if (!properties.isEnabled()) {
            throw new ChaosDisabledException();
        }
    }

    @GetMapping("/slow-brew")
    public Map<String, Object> slowBrew(@RequestParam(defaultValue = "3000") long delayMs) {
        long actualDelay = brewingService.slowBrew(delayMs);
        return Map.of("message", "Slow brew finished", "delayMs", actualDelay);
    }

    @PostMapping("/machine/break")
    public Map<String, Object> breakMachine() {
        chaosState.breakMachine();
        return Map.of("machineBroken", true);
    }

    @PostMapping("/machine/repair")
    public Map<String, Object> repairMachine() {
        chaosState.repairMachine();
        return Map.of("machineBroken", false);
    }

    @PostMapping("/health/readiness/fail")
    public Map<String, Object> failReadiness() {
        chaosState.breakReadiness();
        return Map.of("readinessBroken", true);
    }

    @PostMapping("/health/readiness/recover")
    public Map<String, Object> recoverReadiness() {
        chaosState.recoverReadiness();
        return Map.of("readinessBroken", false);
    }

    @PostMapping("/health/liveness/fail")
    public Map<String, Object> failLiveness() {
        AvailabilityChangeEvent.publish(eventPublisher, this, LivenessState.BROKEN);
        return Map.of("liveness", "BROKEN", "note", "kubelet will restart the pod; state resets on restart");
    }

    @PostMapping("/health/liveness/recover")
    public Map<String, Object> recoverLiveness() {
        AvailabilityChangeEvent.publish(eventPublisher, this, LivenessState.CORRECT);
        return Map.of("liveness", "CORRECT");
    }

    @PostMapping("/memory")
    public Map<String, Object> allocateMemory(@RequestParam(defaultValue = "50") int mb) {
        int totalMb = chaosState.allocateMemory(mb);
        return Map.of("retainedMemoryMb", totalMb, "capMb", properties.getMaxMemoryMb());
    }

    @PostMapping("/memory/release")
    public Map<String, Object> releaseMemory() {
        chaosState.releaseMemory();
        return Map.of("retainedMemoryMb", 0);
    }

    @PostMapping("/cpu")
    public Map<String, Object> burnCpu(@RequestParam(defaultValue = "30") int seconds,
                                       @RequestParam(defaultValue = "2") int threads) {
        int burnSeconds = chaosState.startCpuBurn(seconds, threads);
        return Map.of("burnSeconds", burnSeconds, "activeCpuBurners", chaosState.getActiveCpuBurners());
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return chaosState.status();
    }
}
