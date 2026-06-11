package com.example.devops_project.service;

import com.example.devops_project.exception.OutOfStockException;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    /**
     * Stany początkowe celowo niskie — kilkanaście zamówień wystarczy,
     * żeby zobaczyć błędy 409 (out of stock) na dashboardach.
     */
    private static final Map<String, Integer> DEFAULT_STOCK = Map.of(
            "coffee_beans", 360,
            "water", 2000,
            "milk", 1500,
            "chocolate", 100
    );

    private final ConcurrentHashMap<String, AtomicInteger> stock = new ConcurrentHashMap<>();

    public InventoryService(MeterRegistry meterRegistry) {
        DEFAULT_STOCK.forEach((ingredient, amount) -> {
            AtomicInteger level = new AtomicInteger(amount);
            stock.put(ingredient, level);
            Gauge.builder("coffee.inventory.level", level, AtomicInteger::get)
                    .tag("ingredient", ingredient)
                    .description("Current inventory level per ingredient")
                    .register(meterRegistry);
        });
    }

    public synchronized void consume(Map<String, Integer> ingredients) {
        for (Map.Entry<String, Integer> required : ingredients.entrySet()) {
            AtomicInteger level = stock.get(required.getKey());
            if (level == null || level.get() < required.getValue()) {
                throw new OutOfStockException(required.getKey());
            }
        }
        ingredients.forEach((ingredient, amount) -> stock.get(ingredient).addAndGet(-amount));
    }

    public Map<String, Integer> getStock() {
        Map<String, Integer> snapshot = new TreeMap<>();
        stock.forEach((ingredient, level) -> snapshot.put(ingredient, level.get()));
        return snapshot;
    }

    public Map<String, Integer> restock() {
        DEFAULT_STOCK.forEach((ingredient, amount) -> stock.get(ingredient).set(amount));
        logger.info("Inventory restocked to defaults");
        return getStock();
    }
}
