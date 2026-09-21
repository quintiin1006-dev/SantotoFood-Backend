package com.santotofood.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Order {

    private UUID id;
    private UUID cafeteriaId;
    private UUID clientId;

    private String clientName;
    private String clientDocument;

    private OrderStatus status;

    private BigDecimal total;

    private Instant cancellationDeadline;

    private Instant createdAt;
    private Instant updatedAt;

    private List<OrderItem> items;

    public Order(
            UUID id,
            UUID cafeteriaId,
            UUID clientId,
            String clientName,
            String clientDocument,
            BigDecimal total,
            Instant cancellationDeadline
    ) {
        this.id = id;
        this.cafeteriaId = cafeteriaId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientDocument = clientDocument;
        this.status = OrderStatus.PENDING;
        this.total = total;
        this.cancellationDeadline = cancellationDeadline;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.items = new ArrayList<>();
    }

    public void prepare() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Solo un pedido pendiente puede pasar a preparación"
            );
        }

        status = OrderStatus.PREPARING;
        updatedAt = Instant.now();
    }

    public void markReady() {
        if (status != OrderStatus.PREPARING) {
            throw new IllegalStateException(
                    "Solo un pedido en preparación puede marcarse como listo"
            );
        }

        status = OrderStatus.READY;
        updatedAt = Instant.now();
    }

    public void callStudent() {
        if (status != OrderStatus.READY) {
            throw new IllegalStateException(
                    "Solo un pedido listo puede llamar al estudiante"
            );
        }

        status = OrderStatus.CALLED;
        updatedAt = Instant.now();
    }

    public void deliver() {
        if (status != OrderStatus.CALLED) {
            throw new IllegalStateException(
                    "Solo un pedido llamado puede marcarse como entregado"
            );
        }

        status = OrderStatus.DELIVERED;
        updatedAt = Instant.now();
    }

    public void cancel() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Solo un pedido pendiente puede cancelarse"
            );
        }

        if (cancellationDeadline != null
                && Instant.now().isAfter(cancellationDeadline)) {

            throw new IllegalStateException(
                    "El tiempo para cancelar el pedido ha expirado"
            );
        }

        status = OrderStatus.CANCELLED;
        updatedAt = Instant.now();
    }

    public static Order reconstitute(
            UUID id,
            UUID cafeteriaId,
            UUID clientId,
            String clientName,
            String clientDocument,
            OrderStatus status,
            BigDecimal total,
            Instant cancellationDeadline,
            Instant createdAt,
            Instant updatedAt
    ) {
        Order order = new Order(
                id,
                cafeteriaId,
                clientId,
                clientName,
                clientDocument,
                total,
                cancellationDeadline
        );

        order.status = status;
        order.createdAt = createdAt;
        order.updatedAt = updatedAt;

        return order;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null
                ? new ArrayList<>(items)
                : new ArrayList<>();
    }
}