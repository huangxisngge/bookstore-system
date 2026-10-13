package com.bookstore.dto;

import lombok.Data;

@Data
public class CartAddRequest {

    private Long bookId;
    private Integer quantity = 1;
}
