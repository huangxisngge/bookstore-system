package com.bookstore.controller;

import com.bookstore.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通用接口：服务器时间（毫秒时间戳）。
 * 抢购倒计时以"服务器时间"为唯一闸门，前端据此校准本地时钟偏移，
 * 保证"倒计时归零"即服务端真实放行时刻，避免客户端时钟偏差导致提前/滞后抢购。
 */
@RestController
@RequestMapping("/api/common")
public class CommonController {

    @GetMapping("/time")
    public Result<Long> serverTime() {
        return Result.success(System.currentTimeMillis());
    }
}