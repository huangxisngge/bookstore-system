package com.bookstore.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bookstore.dto.ReturnAuditRequest;
import com.bookstore.dto.ReturnRequest;
import com.bookstore.entity.ReturnRecord;
import com.bookstore.vo.ReturnRecordVO;
import com.bookstore.vo.StatisticsVO;

import java.util.List;
import java.util.Map;

public interface ReturnRecordService extends IService<ReturnRecord> {

    void applyReturn(Long userId, ReturnRequest request);

    void auditReturn(ReturnAuditRequest request);

    Page<ReturnRecordVO> getUserReturns(Long userId, Page<ReturnRecord> page);

    Page<ReturnRecordVO> getAllReturns(Page<ReturnRecord> page, Integer status);

    List<ReturnRecordVO> getPendingByOrderId(Long orderId);

    List<StatisticsVO.BookSalesRanking> getBookSalesRanking(int limit);

    List<StatisticsVO.MonthlySales> getMonthlySalesTrend();

    List<StatisticsVO.CategorySales> getCategorySalesDistribution();

    List<StatisticsVO.InventoryAlert> getInventoryAlert(int threshold);

    List<StatisticsVO.ReturnTrend> getReturnTrend();

    StatisticsVO.OverviewStats getOverviewStats();
}
