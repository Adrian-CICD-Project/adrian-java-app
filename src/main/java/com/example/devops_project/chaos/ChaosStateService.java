package com.example.devops_project.chaos;

import com.example.devops_project.config.ChaosProperties;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Trzyma stan wszystkich przełączników chaosu (in-memory — restart poda zeruje stan).
 */
@Service
public class ChaosStateService {

    private static final Logger logger = LoggerFactory.getLogger(ChaosStateService.class);
    private static final int BYTES_PER_MB = 1024 * 1024;

    private final ChaosProperties properties;

    private final AtomicBoolean machineBroken = new AtomicBoolean(false);
    private final AtomicBoolean readinessBroken = new AtomicBoolean(false);

    private final List<byte[]> memoryHog = new ArrayList<>();
    private final AtomicInteger retainedMb = new AtomicInteger(0);
    private final AtomicInteger activeCpuBurners = new AtomicInteger(0);

    public ChaosStateService(ChaosProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        Gauge.builder("coffee.machine.status", machineBroken, b -> b.get() ? 0.0 : 1.0)
                .description("Coffee machine status: 1 = up, 0 = broken")
                .register(meterRegistry);
        Gauge.builder("coffee.chaos.retained.memory.mb", retainedMb, AtomicInteger::get)
                .description("Memory retained by chaos endpoint in MB")
                .register(meterRegistry);
    }

    // --- ekspres ---

    public void breakMachine() {
        machineBroken.set(true);
        logger.error("Coffee machine has been broken via chaos endpoint - all orders will fail");
    }

    public void repairMachine() {
        machineBroken.set(false);
        logger.info("Coffee machine repaired");
    }

    public boolean isMachineBroken() {
        return machineBroken.get();
    }

    // --- readiness ---

    public void breakReadiness() {
        readinessBroken.set(true);
        logger.error("Readiness sabotaged via chaos endpoint - pod will become NotReady");
    }

    public void recoverReadiness() {
        readinessBroken.set(false);
        logger.info("Readiness recovered");
    }

    public boolean isReadinessBroken() {
        return readinessBroken.get();
    }

    // --- pamięć ---

    public synchronized int allocateMemory(int mb) {
        if (mb < 1) {
            throw new IllegalArgumentException("mb must be >= 1");
        }
        int allowed = Math.min(mb, properties.getMaxMemoryMb() - retainedMb.get());
        if (allowed <= 0) {
            logger.warn("Memory allocation rejected - cap of {} MB reached", properties.getMaxMemoryMb());
            return retainedMb.get();
        }
        for (int i = 0; i < allowed; i++) {
            byte[] chunk = new byte[BYTES_PER_MB];
            // zapis co stronę pamięci, żeby strony zostały faktycznie zaalokowane przez OS
            for (int offset = 0; offset < chunk.length; offset += 4096) {
                chunk[offset] = 1;
            }
            memoryHog.add(chunk);
        }
        int total = retainedMb.addAndGet(allowed);
        logger.warn("Chaos memory allocation: +{} MB (total retained: {} MB)", allowed, total);
        return total;
    }

    public synchronized void releaseMemory() {
        memoryHog.clear();
        retainedMb.set(0);
        logger.info("Chaos memory released");
    }

    public int getRetainedMemoryMb() {
        return retainedMb.get();
    }

    // --- CPU ---

    public int startCpuBurn(int seconds, int threads) {
        int burnSeconds = Math.clamp(seconds, 1, properties.getMaxCpuSeconds());
        int burnThreads = Math.clamp(threads, 1, properties.getMaxCpuThreads());
        long deadline = System.nanoTime() + burnSeconds * 1_000_000_000L;
        for (int i = 0; i < burnThreads; i++) {
            Thread burner = new Thread(() -> {
                activeCpuBurners.incrementAndGet();
                try {
                    long sink = 0;
                    while (System.nanoTime() < deadline) {
                        sink += ThreadLocalRandomHolder.next();
                    }
                    logger.debug("CPU burner finished (sink={})", sink);
                } finally {
                    activeCpuBurners.decrementAndGet();
                }
            }, "chaos-cpu-burner-" + i);
            burner.setDaemon(true);
            burner.start();
        }
        logger.warn("Chaos CPU burn started: {} thread(s) for {} s", burnThreads, burnSeconds);
        return burnSeconds;
    }

    public int getActiveCpuBurners() {
        return activeCpuBurners.get();
    }

    // --- status ---

    public Map<String, Object> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("chaosEnabled", properties.isEnabled());
        status.put("machineBroken", machineBroken.get());
        status.put("readinessBroken", readinessBroken.get());
        status.put("retainedMemoryMb", retainedMb.get());
        status.put("activeCpuBurners", activeCpuBurners.get());
        return status;
    }

    /** Tani generator pracy dla pętli CPU — bez kontencji między wątkami. */
    private static final class ThreadLocalRandomHolder {
        private ThreadLocalRandomHolder() {
        }

        static long next() {
            return java.util.concurrent.ThreadLocalRandom.current().nextLong();
        }
    }
}
