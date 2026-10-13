package com.bookstore.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReturnAuditRequest {

    @NotNull(message = "退货记录ID不能为空")
    private Long returnId;

    @NotNull(message = "审核结果不能为空")
    private Integer status;

    private String adminRemark;
}
