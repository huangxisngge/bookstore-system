package com.bookstore.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReturnRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "订单明细ID不能为空")
    private Long orderItemId;

    @NotNull(message = "退货数量不能为空")
    private Integer quantity;

    private String reason;
}
