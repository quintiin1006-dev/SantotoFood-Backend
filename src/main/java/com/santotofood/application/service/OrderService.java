package com.santotofood.application.service;

import com.santotofood.application.port.in.CallStudentUseCase;
import com.santotofood.application.port.in.CancelOrderUseCase;
import com.santotofood.application.port.in.DeliverOrderUseCase;
import com.santotofood.application.port.in.GetOrdersByCafeteriaUseCase;
import com.santotofood.application.port.in.MarkOrderReadyUseCase;
import com.santotofood.application.port.in.PrepareOrderUseCase;
import com.santotofood.domain.model.Order;
import com.santotofood.domain.model.OrderItem;
import com.santotofood.domain.port.out.OrderItemRepository;
import com.santotofood.domain.port.out.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService implements
        PrepareOrderUseCase,
        MarkOrderReadyUseCase,
        CallStudentUseCase,
        DeliverOrderUseCase,
        CancelOrderUseCase,
        GetOrdersByCafeteriaUseCase {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    @Transactional
    public Order prepareOrder(UUID orderId) {

        Order order = findOrder(orderId);

        Order firstPendingOrder =
                orderRepository
                        .findFirstPendingByCafeteriaId(
                                order.getCafeteriaId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No hay pedidos pendientes"
                                )
                        );

        if (!firstPendingOrder.getId().equals(order.getId())) {
            throw new IllegalStateException(
                    "No puedes preparar este pedido todavía. " +
                            "El pedido anterior debe ser procesado primero."
            );
        }

        order.prepare();

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order markOrderReady(UUID orderId) {

        Order order = findOrder(orderId);

        order.markReady();

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order callStudent(UUID orderId) {

        Order order = findOrder(orderId);

        order.callStudent();

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order deliverOrder(UUID orderId) {

        Order order = findOrder(orderId);

        order.deliver();

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order cancelOrder(UUID orderId) {

        Order order = findOrder(orderId);

        order.cancel();

        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByCafeteria(
            UUID cafeteriaId
    ) {

        List<Order> orders =
                orderRepository.findByCafeteriaId(
                        cafeteriaId
                );

        if (orders.isEmpty()) {
            return orders;
        }

        List<UUID> orderIds =
                orders.stream()
                        .map(Order::getId)
                        .toList();

        List<OrderItem> items =
                orderItemRepository.findByOrderIds(
                        orderIds
                );

        Map<UUID, List<OrderItem>> itemsByOrderId =
                items.stream()
                        .collect(
                                Collectors.groupingBy(
                                        OrderItem::getOrderId
                                )
                        );

        orders.forEach(order ->
                order.setItems(
                        itemsByOrderId.getOrDefault(
                                order.getId(),
                                List.of()
                        )
                )
        );

        return orders;
    }

    private Order findOrder(UUID orderId) {

        return orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pedido no existe"
                        )
                );
    }
}