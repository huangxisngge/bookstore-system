package com.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderCreateRequest {

    @NotBlank(message = "收货地址不能为空")
    private String address;

    @NotBlank(message = "收件人姓名不能为空")
    private String receiverName;

    @NotBlank(message = "收件人电话不能为空")
    private String receiverPhone;

    private String remark;

    private Long cartId;

    /**
     * 一键直购（开抢快速结算）：不为空时跳过购物车，直接对单本图书 bookId 下单 quantity 本
     */
    private Long bookId;

    private Integer quantity;
}
