package com.bookstore.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoginVO {
    private Long userId;
    private String username;
    private String realName;
    private String token;
    private Integer role;
    private Integer memberLevel;
    private BigDecimal memberDiscount;
    private String avatar;
}
