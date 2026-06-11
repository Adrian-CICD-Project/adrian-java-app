package com.example.devops_project.service;

import com.example.devops_project.exception.CoffeeMachineException;
import com.example.devops_project.exception.OutOfStockException;
import com.example.devops_project.model.MenuItem;
import com.example.devops_project.model.Order;
import com.example.devops_project.model.OrderStatus;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final MenuService menuService;
    private final InventoryService inventoryService;
    private final BrewingService brewingService;
    private final MeterRegistry meterRegistry;

    private final ConcurrentLinkedQueue<Order> orders = new ConcurrentLinkedQueue<>();
    private final AtomicLong orderSequence = new AtomicLong(0);

    public OrderService(MenuService menuService, InventoryService inventoryService,
                        BrewingService brewingService, MeterRegistry meterRegistry) {
        this.menuService = menuService;
        this.inventoryService = inventoryService;
        this.brewingService = brewingService;
        this.meterRegistry = meterRegistry;
    }

    public Order placeOrder(String itemId) {
        MenuItem item = menuService.getItem(itemId);
        try {
            inventoryService.consume(item.ingredients());
            brewingService.brew(item);
        } catch (OutOfStockException ex) {
            recordOrder(item, OrderStatus.REJECTED_OUT_OF_STOCK);
            throw ex;
        } catch (CoffeeMachineException ex) {
            recordOrder(item, OrderStatus.FAILED_MACHINE);
            throw ex;
        }
        Order order = recordOrder(item, OrderStatus.COMPLETED);
        logger.info("Order {} completed: {}", order.id(), item.name());
        return order;
    }

    public List<Order> getOrders() {
        return List.copyOf(orders);
    }

    private Order recordOrder(MenuItem item, OrderStatus status) {
        Order order = new Order(orderSequence.incrementAndGet(), item.id(), status, Instant.now());
        orders.add(order);
        meterRegistry.counter("coffee.orders",
                "status", status.name().toLowerCase(),
                "item", item.id()).increment();
        return order;
    }
}
