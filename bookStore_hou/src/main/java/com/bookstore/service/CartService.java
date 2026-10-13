package com.bookstore.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bookstore.entity.Cart;
import com.bookstore.vo.CartVO;

import java.util.List;

public interface CartService extends IService<Cart> {

    List<CartVO> getCartList(Long userId);

    void addToCart(Long userId, Long bookId, Integer quantity);

    void updateQuantity(Long userId, Long cartId, Integer quantity);

    void removeFromCart(Long userId, Long cartId);

    void clearCart(Long userId);

    List<Cart> getCartByUserId(Long userId);
}
