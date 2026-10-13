package com.bookstore.vo;

import com.bookstore.entity.ReturnRecord;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReturnRecordVO {
    private Long id;
    private Long orderId;
    private Long orderItemId;
    private Integer quantity;
    private Long userId;
    private String reason;
    private Integer status;
    private String statusText;
    private String adminRemark;
    private LocalDateTime createTime;
    private String bookTitle;
    private String bookCover;
    private BigDecimal subtotal;

    public static ReturnRecordVO from(ReturnRecord record) {
        ReturnRecordVO vo = new ReturnRecordVO();
        vo.setId(record.getId());
        vo.setOrderId(record.getOrderId());
        vo.setOrderItemId(record.getOrderItemId());
        vo.setQuantity(record.getQuantity());
        vo.setUserId(record.getUserId());
        vo.setReason(record.getReason());
        vo.setStatus(record.getStatus());
        vo.setStatusText(statusText(record.getStatus()));
        vo.setAdminRemark(record.getAdminRemark());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }

    private static String statusText(Integer status) {
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "审核通过";
            case 2 -> "审核拒绝";
            default -> "未知";
        };
    }
}
