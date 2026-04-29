package com.nongzhushou.order.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.order.dto.OrderFinishConfirmRequest;
import com.nongzhushou.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(orderService.list());
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable @Positive(message = "id must be greater than 0") Long id) {
        return Result.ok(orderService.detail(id));
    }

    @PostMapping("/{id}/confirm-finish")
    public Result<Map<String, Object>> confirmFinish(
            @PathVariable @Positive(message = "id must be greater than 0") Long id,
            @Valid @RequestBody OrderFinishConfirmRequest request
    ) {
        return Result.ok(orderService.confirmFinish(id, request.getActorRole()));
    }
}
