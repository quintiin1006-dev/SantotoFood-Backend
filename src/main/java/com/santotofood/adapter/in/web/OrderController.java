package com.santotofood.adapter.in.web;

import com.santotofood.application.port.in.CallStudentUseCase;
import com.santotofood.application.port.in.CancelOrderUseCase;
import com.santotofood.application.port.in.DeliverOrderUseCase;
import com.santotofood.application.port.in.GetOrdersByCafeteriaUseCase;
import com.santotofood.application.port.in.MarkOrderReadyUseCase;
import com.santotofood.application.port.in.PrepareOrderUseCase;
import com.santotofood.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PrepareOrderUseCase prepareOrderUseCase;
    private final MarkOrderReadyUseCase markOrderReadyUseCase;
    private final CallStudentUseCase callStudentUseCase;
    private final DeliverOrderUseCase deliverOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final GetOrdersByCafeteriaUseCase getOrdersByCafeteriaUseCase;


    @GetMapping
    public ResponseEntity<List<Order>> getOrdersByCafeteria(
            @RequestParam UUID cafeteriaId
    ) {

        List<Order> orders =
                getOrdersByCafeteriaUseCase
                        .getOrdersByCafeteria(cafeteriaId);

        return ResponseEntity.ok(orders);
    }


    @PatchMapping("/{orderId}/prepare")
    public ResponseEntity<Order> prepareOrder(
            @PathVariable UUID orderId
    ) {

        Order updatedOrder =
                prepareOrderUseCase.prepareOrder(orderId);

        return ResponseEntity.ok(updatedOrder);
    }


    @PatchMapping("/{orderId}/ready")
    public ResponseEntity<Order> markOrderReady(
            @PathVariable UUID orderId
    ) {

        Order updatedOrder =
                markOrderReadyUseCase.markOrderReady(orderId);

        return ResponseEntity.ok(updatedOrder);
    }


    @PatchMapping("/{orderId}/call")
    public ResponseEntity<Order> callStudent(
            @PathVariable UUID orderId
    ) {

        Order updatedOrder =
                callStudentUseCase.callStudent(orderId);

        return ResponseEntity.ok(updatedOrder);
    }


    @PatchMapping("/{orderId}/deliver")
    public ResponseEntity<Order> deliverOrder(
            @PathVariable UUID orderId
    ) {

        Order updatedOrder =
                deliverOrderUseCase.deliverOrder(orderId);

        return ResponseEntity.ok(updatedOrder);
    }


    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable UUID orderId
    ) {

        Order updatedOrder =
                cancelOrderUseCase.cancelOrder(orderId);

        return ResponseEntity.ok(updatedOrder);
    }


    /**
     * Errores de reglas de negocio.
     *
     * Por ejemplo:
     * - Intentar preparar un pedido que no tiene prioridad.
     * - Intentar entregar un pedido que no fue llamado.
     * - Intentar cambiar un pedido en un estado incorrecto.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(
            IllegalStateException exception
    ) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }


    /**
     * Pedido inexistente.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }
}