package com.bookstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookstore.entity.ReturnRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReturnRecordMapper extends BaseMapper<ReturnRecord> {

    /**
     * 按订单统计各明细已产生的退货数量（含待审核+已通过）。
     * 返回: order_item_id, quantity(该明细累计退货数)
     */
    @Select("SELECT order_item_id, SUM(quantity) AS returnedQty " +
            "FROM return_record " +
            "WHERE order_id = #{orderId} AND deleted = 0 AND status IN (0, 1) " +
            "GROUP BY order_item_id")
    List<Map<String, Object>> selectReturnedQtyByOrder(@Param("orderId") Long orderId);

    /**
     * 统计某订单已审核通过(approved=1)的退货总额与订单是否完全退完。
     * 返回记录: status(审核结果), order_item_id, qty, fullyReturned
     * 这里用两个查询分别提供"总通过量"与"明细维度已退量"。
     */
    @Select("SELECT order_item_id, SUM(quantity) AS approvedQty " +
            "FROM return_record " +
            "WHERE order_id = #{orderId} AND deleted = 0 AND status = 1 " +
            "GROUP BY order_item_id")
    List<Map<String, Object>> selectApprovedQtyByOrder(@Param("orderId") Long orderId);

    @Select("SELECT COUNT(*) FROM return_record " +
            "WHERE order_item_id = #{orderItemId} AND deleted = 0 AND status = 0")
    Long countPendingByItem(@Param("orderItemId") Long orderItemId);

    @Select("SELECT COUNT(*) FROM return_record " +
            "WHERE order_id = #{orderId} AND deleted = 0 AND status = 0")
    Long countPendingByOrderId(@Param("orderId") Long orderId);
}
