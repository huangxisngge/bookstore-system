package com.bookstore.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private BigDecimal memberDiscount;
    private BigDecimal fullReductionAmount;
    private Integer status;
    private String statusText;
    /** 订单级退货派生状态：0-无退货 1-退货审核中 2-部分退货 3-已全部退货 */
    private Integer returnState;
    private String returnStateText;
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime receiveTime;
    private LocalDateTime returnTime;
    private String address;
    private String receiverName;
    private String receiverPhone;
    private String remark;
    private LocalDateTime createTime;
    private List<OrderItemVO> items;

    @Data
    public static class OrderItemVO {
        private Long id;
        private Long orderId;
        private Long bookId;
        private String bookTitle;
        private String bookCover;
        private BigDecimal price;
        private BigDecimal discount;
        private BigDecimal promoPrice;
        private Integer quantity;
        private BigDecimal subtotal;
        /** 该明细累计已退数量（待审核+已通过） */
        private Integer returnedQty;
        /** 该明细当前还可退数量 */
        private Integer returnableQty;
    }
}
