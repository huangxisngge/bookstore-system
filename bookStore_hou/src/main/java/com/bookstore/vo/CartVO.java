package com.bookstore.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartVO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookCover;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal finalPrice;
    private BigDecimal promoPrice;
    private LocalDateTime promoStartTime;
    private LocalDateTime promoEndTime;
    private Boolean onPromo;
    private LocalDateTime grabStartTime;
    private Boolean grabNotStarted;
    private Long grabRemainSeconds;
    private Integer quantity;
    private BigDecimal subtotal;
    private Integer stock;
}
