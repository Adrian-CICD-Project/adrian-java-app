package com.example.devops_project;

import com.example.devops_project.chaos.ChaosStateService;
import com.example.devops_project.config.ChaosProperties;
import com.example.devops_project.exception.CoffeeMachineException;
import com.example.devops_project.exception.MenuItemNotFoundException;
import com.example.devops_project.exception.OutOfStockException;
import com.example.devops_project.model.Order;
import com.example.devops_project.model.OrderStatus;
import com.example.devops_project.service.BrewingService;
import com.example.devops_project.service.InventoryService;
import com.example.devops_project.service.MenuService;
import com.example.devops_project.service.OrderService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderServiceTest {

    private SimpleMeterRegistry meterRegistry;
    private ChaosStateService chaosState;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        ChaosProperties properties = new ChaosProperties();
        chaosState = new ChaosStateService(properties, meterRegistry);
        MenuService menuService = new MenuService();
        InventoryService inventoryService = new InventoryService(meterRegistry);
        BrewingService brewingService = new BrewingService(chaosState, properties, meterRegistry);
        orderService = new OrderService(menuService, inventoryService, brewingService, meterRegistry);
    }

    @Test
    void placeOrder_ShouldCompleteAndIncrementCounter() {
        Order order = orderService.placeOrder("espresso");

        assertThat(order.status()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(meterRegistry.counter("coffee.orders", "status", "completed", "item", "espresso").count())
                .isEqualTo(1.0);
        assertThat(orderService.getOrders()).hasSize(1);
    }

    @Test
    void placeOrder_UnknownItem_ShouldThrowNotFound() {
        assertThatThrownBy(() -> orderService.placeOrder("tea"))
                .isInstanceOf(MenuItemNotFoundException.class);
        assertThat(orderService.getOrders()).isEmpty();
    }

    @Test
    void placeOrder_OutOfStock_ShouldThrowAndCountRejection() {
        // mocha zużywa 20 czekolady, stan to 100 — szóste zamówienie przekracza stan
        for (int i = 0; i < 5; i++) {
            orderService.placeOrder("mocha");
        }
        assertThatThrownBy(() -> orderService.placeOrder("mocha"))
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining("chocolate");
        assertThat(meterRegistry.counter("coffee.orders", "status", "rejected_out_of_stock", "item", "mocha").count())
                .isEqualTo(1.0);
    }

    @Test
    void placeOrder_MachineBroken_ShouldThrowAndCountFailure() {
        chaosState.breakMachine();

        assertThatThrownBy(() -> orderService.placeOrder("latte"))
                .isInstanceOf(CoffeeMachineException.class);
        assertThat(meterRegistry.counter("coffee.orders", "status", "failed_machine", "item", "latte").count())
                .isEqualTo(1.0);
    }
}
