package com.bookstore.controller;

import com.bookstore.common.Result;
import com.bookstore.service.ReturnRecordService;
import com.bookstore.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final ReturnRecordService returnRecordService;

    @GetMapping("/overview")
    public Result<StatisticsVO.OverviewStats> getOverviewStats() {
        return Result.success(returnRecordService.getOverviewStats());
    }

    @GetMapping("/sales-ranking")
    public Result<List<StatisticsVO.BookSalesRanking>> getBookSalesRanking(
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(returnRecordService.getBookSalesRanking(limit));
    }

    @GetMapping("/monthly-trend")
    public Result<List<StatisticsVO.MonthlySales>> getMonthlySalesTrend() {
        return Result.success(returnRecordService.getMonthlySalesTrend());
    }

    @GetMapping("/category-distribution")
    public Result<List<StatisticsVO.CategorySales>> getCategorySalesDistribution() {
        return Result.success(returnRecordService.getCategorySalesDistribution());
    }

    @GetMapping("/inventory-alert")
    public Result<List<StatisticsVO.InventoryAlert>> getInventoryAlert(
            @RequestParam(defaultValue = "10") int threshold) {
        return Result.success(returnRecordService.getInventoryAlert(threshold));
    }

    @GetMapping("/return-trend")
    public Result<List<StatisticsVO.ReturnTrend>> getReturnTrend() {
        return Result.success(returnRecordService.getReturnTrend());
    }
}
