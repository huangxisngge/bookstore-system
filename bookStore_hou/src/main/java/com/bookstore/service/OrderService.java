package com.bookstore.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bookstore.dto.OrderCreateRequest;
import com.bookstore.entity.Order;
import com.bookstore.vo.DeliveryInfo;
import com.bookstore.vo.OrderVO;

import java.util.List;

public interface OrderService extends IService<Order> {

    OrderVO createOrder(Long userId, OrderCreateRequest request);

    /**
     * 历史收货地址模板（按最近使用去重，最多 6 条），结算页一键复用
     */
    List<DeliveryInfo> getRecentAddresses(Long userId);

    OrderVO getOrderDetail(Long userId, Long orderId);

    Page<OrderVO> getUserOrders(Long userId, Page<Order> page, Integer status);

    Page<OrderVO> getAllOrders(Page<Order> page, Integer status, String keyword);

    void payOrder(Long userId, Long orderId);

    void shipOrder(Long orderId);

    void receiveOrder(Long userId, Long orderId);

    void cancelOrder(Long userId, Long orderId);

    void updateStatus(Long orderId, Integer status);
}
