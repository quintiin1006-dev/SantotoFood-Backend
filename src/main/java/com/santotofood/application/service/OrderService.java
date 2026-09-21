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
import java.util.UUID;

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

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pedido no existe"
                        )
                );

        Order firstPendingOrder =
                orderRepository.findFirstPendingByCafeteriaId(
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
    public Order markOrderReady(UUID orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pedido no existe"
                        )
                );

        order.markReady();

        return orderRepository.save(order);
    }

    @Override
    public Order callStudent(UUID orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pedido no existe"
                        )
                );

        order.callStudent();

        return orderRepository.save(order);
    }

    @Override
    public Order deliverOrder(UUID orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pedido no existe"
                        )
                );

        order.deliver();

        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(UUID orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pedido no existe"
                        )
                );

        order.cancel();

        return orderRepository.save(order);
    }

    @Override
    public List<Order> getOrdersByCafeteria(UUID cafeteriaId) {

        List<Order> orders =
                orderRepository.findByCafeteriaId(cafeteriaId);

        orders.forEach(order -> {

            List<OrderItem> items =
                    orderItemRepository.findByOrderId(
                            order.getId()
                    );

            order.setItems(items);
        });

        return orders;
    }
}