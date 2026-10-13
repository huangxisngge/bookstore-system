package com.bookstore.controller;

import com.bookstore.common.Result;
import com.bookstore.dto.CartAddRequest;
import com.bookstore.service.CartService;
import com.bookstore.vo.CartVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/list")
    public Result<List<CartVO>> getCartList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(cartService.getCartList(userId));
    }

    @PostMapping("/add")
    public Result<?> addToCart(@RequestBody CartAddRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        cartService.addToCart(userId, request.getBookId(), request.getQuantity());
        return Result.success("添加成功");
    }

    @PutMapping("/update/{cartId}")
    public Result<?> updateQuantity(@PathVariable Long cartId,
                                    @RequestParam Integer quantity,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.updateQuantity(userId, cartId, quantity);
        return Result.success("更新成功");
    }

    @DeleteMapping("/remove/{cartId}")
    public Result<?> removeFromCart(@PathVariable Long cartId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.removeFromCart(userId, cartId);
        return Result.success("删除成功");
    }

    @DeleteMapping("/clear")
    public Result<?> clearCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.clearCart(userId);
        return Result.success("清空成功");
    }
}
