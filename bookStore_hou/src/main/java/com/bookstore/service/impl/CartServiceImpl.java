package com.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.entity.Book;
import com.bookstore.entity.Cart;
import com.bookstore.mapper.CartMapper;
import com.bookstore.service.BookService;
import com.bookstore.service.CartService;
import com.bookstore.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final BookService bookService;

    @Override
    public List<CartVO> getCartList(Long userId) {
        List<CartVO> list = this.getBaseMapper().selectCartVOList(userId);
        list.forEach(this::fillDerivedFields);
        return list;
    }

    @Override
    public void addToCart(Long userId, Long bookId, Integer quantity) {
        Book book = bookService.getById(bookId);
        if (book == null || book.getStatus() != 1) {
            throw new BusinessException("图书不存在或已下架");
        }
        if (bookService.isGrabNotStarted(book)) {
            throw new BusinessException(409,
                    "「" + book.getTitle() + "」尚未开抢，" + grabRemainText(book) + "，到点后可加入购物车");
        }
        if (book.getStock() <= 0) {
            throw new BusinessException(409, "该图书已被抢光，无法加入购物车");
        }
        if (book.getStock() < quantity) {
            throw new BusinessException(409, "库存不足，当前仅剩 " + book.getStock() + " 件");
        }

        // 查询时忽略软删除，处理已删除但唯一约束仍存在的记录
        Cart existingCart = this.getBaseMapper().selectIncludingDeleted(userId, bookId);
        if (existingCart != null) {
            int newQuantity;
            if (existingCart.getDeleted() != null && existingCart.getDeleted() == 1) {
                newQuantity = quantity;
            } else {
                newQuantity = existingCart.getQuantity() + quantity;
            }
            if (newQuantity > book.getStock()) {
                throw new BusinessException(409, "库存不足，当前仅剩 " + book.getStock() + " 件");
            }
            this.getBaseMapper().restoreAndUpdate(existingCart.getId(), newQuantity);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setBookId(bookId);
            cart.setQuantity(quantity);
            this.save(cart);
        }
    }

    @Override
    public void updateQuantity(Long userId, Long cartId, Integer quantity) {
        Cart cart = this.getById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车记录不存在");
        }
        Book book = bookService.getById(cart.getBookId());
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        if (quantity <= 0) {
            this.removeById(cartId);
            return;
        }
        if (quantity > book.getStock()) {
            throw new BusinessException("库存不足");
        }
        cart.setQuantity(quantity);
        this.updateById(cart);
    }

    @Override
    public void removeFromCart(Long userId, Long cartId) {
        Cart cart = this.getById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车记录不存在");
        }
        this.removeById(cartId);
    }

    @Override
    public void clearCart(Long userId) {
        this.remove(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
    }

    @Override
    public List<Cart> getCartByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
    }

    /**
     * 促销状态/到手价/小计依赖当前时间与购买数量，属于计算字段，在服务层填充
     */
    private void fillDerivedFields(CartVO vo) {
        LocalDateTime now = LocalDateTime.now();
        boolean onPromo = vo.getPromoStartTime() != null && vo.getPromoEndTime() != null
                && vo.getPromoPrice() != null
                && !now.isBefore(vo.getPromoStartTime())
                && !now.isAfter(vo.getPromoEndTime());

        vo.setOnPromo(onPromo);
        BigDecimal unitPrice = onPromo ? vo.getPromoPrice()
                : vo.getPrice().multiply(vo.getDiscount());
        vo.setFinalPrice(unitPrice);
        vo.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(vo.getQuantity())));

        // 开抢倒计时：未开抢的条目在购物车中禁止结算，前端据此展示倒计时
        boolean grabNotStarted = vo.getGrabStartTime() != null
                && now.isBefore(vo.getGrabStartTime());
        vo.setGrabNotStarted(grabNotStarted);
        vo.setGrabRemainSeconds(grabNotStarted
                ? Math.max(0L, Duration.between(now, vo.getGrabStartTime()).getSeconds())
                : 0L);
    }

    /**
     * 距开抢还剩余的"x分x秒"文案
     */
    private String grabRemainText(Book book) {
        long seconds = Math.max(0L, Duration.between(LocalDateTime.now(), book.getGrabStartTime()).getSeconds());
        if (seconds <= 0) {
            return "即将开抢";
        }
        long minutes = seconds / 60;
        long remainSeconds = seconds % 60;
        return minutes > 0 ? "约 " + minutes + " 分 " + remainSeconds + " 秒后开抢" : remainSeconds + " 秒后开抢";
    }

}
