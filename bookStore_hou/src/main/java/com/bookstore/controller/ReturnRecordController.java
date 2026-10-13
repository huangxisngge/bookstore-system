package com.bookstore.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.common.PageResult;
import com.bookstore.common.Result;
import com.bookstore.dto.ReturnAuditRequest;
import com.bookstore.dto.ReturnRequest;
import com.bookstore.entity.ReturnRecord;
import com.bookstore.service.ReturnRecordService;
import com.bookstore.vo.ReturnRecordVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/return")
@RequiredArgsConstructor
public class ReturnRecordController {

    private final ReturnRecordService returnRecordService;

    @PostMapping("/apply")
    public Result<?> applyReturn(@RequestBody ReturnRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        returnRecordService.applyReturn(userId, request);
        return Result.success("退货申请已提交");
    }

    @PostMapping("/audit")
    public Result<?> auditReturn(@RequestBody ReturnAuditRequest request) {
        returnRecordService.auditReturn(request);
        return Result.success("审核完成");
    }

    @GetMapping("/my")
    public Result<PageResult<ReturnRecordVO>> getUserReturns(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Page<ReturnRecord> page = new Page<>(current, size);
        Page<ReturnRecordVO> result = returnRecordService.getUserReturns(userId, page);
        return Result.success(PageResult.from(result));
    }

    @GetMapping("/list")
    public Result<PageResult<ReturnRecordVO>> getAllReturns(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Page<ReturnRecord> page = new Page<>(current, size);
        Page<ReturnRecordVO> result = returnRecordService.getAllReturns(page, status);
        return Result.success(PageResult.from(result));
    }

    @GetMapping("/pending/{orderId}")
    public Result<List<ReturnRecordVO>> getPendingReturn(@PathVariable Long orderId) {
        return Result.success(returnRecordService.getPendingByOrderId(orderId));
    }
}
