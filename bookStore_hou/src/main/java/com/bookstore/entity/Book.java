package com.bookstore.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("book")
public class Book {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String author;

    private String isbn;

    private String publisher;

    private Long categoryId;

    private BigDecimal price;

    private BigDecimal discount;

    private LocalDateTime promoStartTime;

    private LocalDateTime promoEndTime;

    private LocalDateTime grabStartTime;

    private BigDecimal promoPrice;

    private Integer stock;

    private Integer sales;

    private String coverImage;

    private String description;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
