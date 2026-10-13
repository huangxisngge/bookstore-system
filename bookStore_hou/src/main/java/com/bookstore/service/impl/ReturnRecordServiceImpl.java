package com.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.dto.ReturnAuditRequest;
import com.bookstore.dto.ReturnRequest;
import com.bookstore.entity.Order;
import com.bookstore.entity.OrderItem;
import com.bookstore.entity.ReturnRecord;
import com.bookstore.mapper.OrderMapper;
import com.bookstore.mapper.ReturnRecordMapper;
import com.bookstore.service.BookService;
import com.bookstore.service.OrderItemService;
import com.bookstore.service.OrderService;
import com.bookstore.service.ReturnRecordService;
import com.bookstore.service.UserService;
import com.bookstore.vo.ReturnRecordVO;
import com.bookstore.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReturnRecordServiceImpl extends ServiceImpl<ReturnRecordMapper, ReturnRecord> implements ReturnRecordService {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final BookService bookService;
    private final UserService userService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyReturn(Long userId, ReturnRequest request) {
        Order order = orderService.getById(request.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1 && order.getStatus() != 3) {
            throw new BusinessException("只有已支付或已完成的订单可以申请退货");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BusinessException("退货数量必须大于0");
        }

        // 校验该明细确属该订单
        OrderItem item = orderItemService.getById(request.getOrderItemId());
        if (item == null || !item.getOrderId().equals(request.getOrderId())) {
            throw new BusinessException("订单明细不存在");
        }

        // 已退数量（待审核 + 已通过）不能超过购买数量，支持同一本书多次分批退
        int returned = this.baseMapper.selectReturnedQtyByOrder(request.getOrderId()).stream()
                .filter(m -> ((Number) m.get("order_item_id")).longValue() == item.getId().longValue())
                .map(m -> ((Number) m.get("returnedQty")).intValue())
                .findFirst().orElse(0);
        int remaining = item.getQuantity() - returned;
        if (request.getQuantity() > remaining) {
            throw new BusinessException("退货数量超过可退数量（最多还可退 " + remaining + " 件）");
        }

        ReturnRecord record = new ReturnRecord();
        record.setOrderId(request.getOrderId());
        record.setOrderItemId(item.getId());
        record.setQuantity(request.getQuantity());
        record.setUserId(userId);
        record.setReason(request.getReason());
        record.setStatus(0);
        this.save(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditReturn(ReturnAuditRequest request) {
        ReturnRecord record = this.getById(request.getReturnId());
        if (record == null) {
            throw new BusinessException("退货记录不存在");
        }
        if (record.getStatus() != 0) {
            throw new BusinessException("该退货记录已审核");
        }

        record.setStatus(request.getStatus());
        record.setAdminRemark(request.getAdminRemark());
        this.updateById(record);

        Order order = orderService.getById(record.getOrderId());
        if (request.getStatus() == 1) {
            // 同意退货：仅回补该明细、该数量的库存
            if (record.getOrderItemId() != null && record.getQuantity() != null) {
                OrderItem item = orderItemService.getById(record.getOrderItemId());
                if (item != null) {
                    bookService.restoreStock(item.getBookId(), record.getQuantity());
                }
            }
            if (order != null) {
                order.setReturnTime(LocalDateTime.now());
                orderService.updateById(order);
            }
        }
        // status=0 待审核 无处理；status=2 拒绝退货：不回补库存、订单保持原流转状态
    }

    @Override
    public Page<ReturnRecordVO> getUserReturns(Long userId, Page<ReturnRecord> page) {
        Page<ReturnRecord> result = this.page(page, new LambdaQueryWrapper<ReturnRecord>()
                .eq(ReturnRecord::getUserId, userId)
                .orderByDesc(ReturnRecord::getCreateTime));
        return toVOPage(result);
    }

    @Override
    public Page<ReturnRecordVO> getAllReturns(Page<ReturnRecord> page, Integer status) {
        LambdaQueryWrapper<ReturnRecord> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ReturnRecord::getStatus, status);
        }
        wrapper.orderByDesc(ReturnRecord::getCreateTime);
        Page<ReturnRecord> result = this.page(page, wrapper);
        return toVOPage(result);
    }

    @Override
    public List<ReturnRecordVO> getPendingByOrderId(Long orderId) {
        List<ReturnRecord> list = this.list(new LambdaQueryWrapper<ReturnRecord>()
                .eq(ReturnRecord::getOrderId, orderId)
                .eq(ReturnRecord::getStatus, 0)
                .orderByAsc(ReturnRecord::getId));
        return enrich(list);
    }

    private Page<ReturnRecordVO> toVOPage(Page<ReturnRecord> result) {
        Page<ReturnRecordVO> voPage =
                new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(enrich(result.getRecords()));
        return voPage;
    }

    private List<ReturnRecordVO> enrich(List<ReturnRecord> records) {
        if (records == null || records.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        Map<Long, OrderItem> itemMap = orderItemService.listByIds(
                        records.stream().map(ReturnRecord::getOrderItemId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(OrderItem::getId, i -> i));
        return records.stream().map(r -> {
            ReturnRecordVO vo = ReturnRecordVO.from(r);
            OrderItem item = itemMap.get(r.getOrderItemId());
            if (item != null) {
                vo.setBookTitle(item.getBookTitle());
                vo.setBookCover(item.getBookCover());
                vo.setSubtotal(item.getPrice()
                        .multiply(BigDecimal.valueOf(r.getQuantity())).setScale(2, RoundingMode.HALF_UP));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<StatisticsVO.BookSalesRanking> getBookSalesRanking(int limit) {
        List<Map<String, Object>> data = orderMapper.getBookSalesRanking(limit);
        return data.stream().map(item -> {
            StatisticsVO.BookSalesRanking vo = new StatisticsVO.BookSalesRanking();
            vo.setBookName((String) item.get("bookName"));
            vo.setTotalSales(((Number) item.get("totalSales")).longValue());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<StatisticsVO.MonthlySales> getMonthlySalesTrend() {
        List<Map<String, Object>> data = orderMapper.getMonthlySalesTrend();
        return data.stream().map(item -> {
            StatisticsVO.MonthlySales vo = new StatisticsVO.MonthlySales();
            vo.setMonth((String) item.get("month"));
            vo.setTotalAmount(((Number) item.get("totalAmount")).doubleValue());
            vo.setOrderCount(((Number) item.get("orderCount")).longValue());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<StatisticsVO.CategorySales> getCategorySalesDistribution() {
        List<Map<String, Object>> data = orderMapper.getCategorySalesDistribution();
        return data.stream().map(item -> {
            StatisticsVO.CategorySales vo = new StatisticsVO.CategorySales();
            vo.setCategoryName((String) item.get("categoryName"));
            vo.setTotalSales(((Number) item.get("totalSales")).doubleValue());
            vo.setOrderCount(((Number) item.get("orderCount")).longValue());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<StatisticsVO.InventoryAlert> getInventoryAlert(int threshold) {
        return bookService.getLowStockBooks(threshold).stream().map(book -> {
            StatisticsVO.InventoryAlert vo = new StatisticsVO.InventoryAlert();
            vo.setId(book.getId());
            vo.setTitle(book.getTitle());
            vo.setAuthor(book.getAuthor());
            vo.setStock(book.getStock());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public StatisticsVO.OverviewStats getOverviewStats() {
        StatisticsVO.OverviewStats stats = new StatisticsVO.OverviewStats();

        stats.setTotalUsers(userService.count());

        stats.setTotalBooks((long) bookService.list().size());

        stats.setTotalOrders(orderService.count());

        LambdaQueryWrapper<Order> pendingWrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 0);
        stats.setPendingOrders(orderService.count(pendingWrapper));

        List<Map<String, Object>> monthlyData = orderMapper.getMonthlySalesTrend();
        double totalRevenue = monthlyData.stream()
                .mapToDouble(item -> ((Number) item.get("totalAmount")).doubleValue())
                .sum();
        stats.setTotalRevenue(totalRevenue);

        // 退货统计改为基于 return_record（支持部分退货后不再用订单状态 4/5 标记）
        stats.setReturnOrders(this.count(new LambdaQueryWrapper<ReturnRecord>()
                .eq(ReturnRecord::getStatus, 1)));

        List<Map<String, Object>> returnData = orderMapper.getReturnTrend();
        double returnAmount = returnData.stream()
                .mapToDouble(item -> ((Number) item.get("returnAmount")).doubleValue())
                .sum();
        stats.setReturnAmount(returnAmount);

        stats.setLowStockBooks((long) bookService.getLowStockBooks(10).size());

        return stats;
    }

    @Override
    public List<StatisticsVO.ReturnTrend> getReturnTrend() {
        List<Map<String, Object>> data = orderMapper.getReturnTrend();
        return data.stream().map(item -> {
            StatisticsVO.ReturnTrend vo = new StatisticsVO.ReturnTrend();
            vo.setMonth((String) item.get("month"));
            vo.setReturnCount(((Number) item.get("returnCount")).longValue());
            vo.setReturnAmount(((Number) item.get("returnAmount")).doubleValue());
            return vo;
        }).collect(Collectors.toList());
    }
}
