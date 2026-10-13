package com.bookstore.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookVO {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private Long categoryId;
    private String categoryName;
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
    private Integer stock;
    private Integer sales;
    private String coverImage;
    private String description;
    private Integer status;
}
