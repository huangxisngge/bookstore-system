package com.bookstore.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.common.PageResult;
import com.bookstore.common.Result;
import com.bookstore.dto.OrderCreateRequest;
import com.bookstore.entity.Order;
import com.bookstore.service.OrderService;
import com.bookstore.vo.DeliveryInfo;
import com.bookstore.vo.OrderVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderCreateRequest request,
                                       HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success("下单成功", orderService.createOrder(userId, request));
    }

    @GetMapping("/address-list")
    public Result<java.util.List<DeliveryInfo>> getRecentAddresses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.getRecentAddresses(userId));
    }

    @GetMapping("/detail/{orderId}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.getOrderDetail(userId, orderId));
    }

    @GetMapping("/my")
    public Result<PageResult<OrderVO>> getUserOrders(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Page<Order> page = new Page<>(current, size);
        Page<OrderVO> result = orderService.getUserOrders(userId, page, status);
        return Result.success(PageResult.from(result));
    }

    @GetMapping("/list")
    public Result<PageResult<OrderVO>> getAllOrders(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Page<Order> page = new Page<>(current, size);
        Page<OrderVO> result = orderService.getAllOrders(page, status, keyword);
        return Result.success(PageResult.from(result));
    }

    @PostMapping("/pay/{orderId}")
    public Result<?> payOrder(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.payOrder(userId, orderId);
        return Result.success("支付成功");
    }

    @PostMapping("/ship/{orderId}")
    public Result<?> shipOrder(@PathVariable Long orderId) {
        orderService.shipOrder(orderId);
        return Result.success("发货成功");
    }

    @PostMapping("/receive/{orderId}")
    public Result<?> receiveOrder(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.receiveOrder(userId, orderId);
        return Result.success("确认收货成功");
    }

    @PostMapping("/cancel/{orderId}")
    public Result<?> cancelOrder(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.cancelOrder(userId, orderId);
        return Result.success("取消成功");
    }
}
