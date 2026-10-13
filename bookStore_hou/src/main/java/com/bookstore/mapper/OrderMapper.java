package com.bookstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookstore.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT b.title AS bookName, SUM(oi.quantity) AS totalSales " +
            "FROM order_item oi " +
            "JOIN `order` o ON oi.order_id = o.id " +
            "JOIN book b ON oi.book_id = b.id " +
            "WHERE o.deleted = 0 AND oi.deleted = 0 AND b.deleted = 0 " +
            "AND o.status IN (1, 2, 3) " +
            "GROUP BY b.title " +
            "ORDER BY totalSales DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getBookSalesRanking(@Param("limit") int limit);

    @Select("SELECT DATE_FORMAT(o.create_time, '%Y-%m') AS month, " +
            "SUM(o.pay_amount) AS totalAmount, " +
            "COUNT(*) AS orderCount " +
            "FROM `order` o " +
            "WHERE o.deleted = 0 AND o.status IN (1, 2, 3) " +
            "AND o.create_time >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
            "GROUP BY DATE_FORMAT(o.create_time, '%Y-%m') " +
            "ORDER BY month")
    List<Map<String, Object>> getMonthlySalesTrend();

    @Select("SELECT c.name AS categoryName, SUM(oi.subtotal) AS totalSales, " +
            "COUNT(DISTINCT oi.order_id) AS orderCount " +
            "FROM order_item oi " +
            "JOIN `order` o ON oi.order_id = o.id " +
            "JOIN book b ON oi.book_id = b.id " +
            "JOIN category c ON b.category_id = c.id " +
            "WHERE o.deleted = 0 AND oi.deleted = 0 AND b.deleted = 0 AND c.deleted = 0 " +
            "AND o.status IN (1, 2, 3) " +
            "GROUP BY c.name " +
            "ORDER BY totalSales DESC")
    List<Map<String, Object>> getCategorySalesDistribution();

    @Select("SELECT DATE_FORMAT(rr.create_time, '%Y-%m') AS month, " +
            "COUNT(*) AS returnCount, " +
            "SUM(oi.subtotal) AS returnAmount " +
            "FROM return_record rr " +
            "JOIN order_item oi ON oi.id = rr.order_item_id " +
            "WHERE rr.deleted = 0 AND rr.status = 1 " +
            "AND rr.create_time >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
            "GROUP BY DATE_FORMAT(rr.create_time, '%Y-%m') " +
            "ORDER BY month")
    List<Map<String, Object>> getReturnTrend();
}
