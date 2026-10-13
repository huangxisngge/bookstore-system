package com.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookstore.common.exception.BusinessException;
import com.bookstore.dto.OrderCreateRequest;
import com.bookstore.entity.*;
import com.bookstore.mapper.OrderMapper;
import com.bookstore.mapper.ReturnRecordMapper;
import com.bookstore.service.*;
import com.bookstore.vo.DeliveryInfo;
import com.bookstore.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderItemService orderItemService;
    private final CartService cartService;
    private final BookService bookService;
    private final UserService userService;
    private final ReturnRecordMapper returnRecordMapper;

    /**
     * 库存冲突专用业务码：前端据此展示"没抢到/已售罄"类提示并阻止后续支付
     */
    public static final int STOCK_CONFLICT_CODE = 409;

    /** 管理端筛选用特殊值：待退货审核（订单退货为派生状态，无独立订单状态码） */
    public static final int STATUS_RETURN_PENDING = 10;

    private static final BigDecimal FULL_REDUCTION_THRESHOLD_1 = new BigDecimal("100");
    private static final BigDecimal FULL_REDUCTION_AMOUNT_1 = new BigDecimal("10");
    private static final BigDecimal FULL_REDUCTION_THRESHOLD_2 = new BigDecimal("200");
    private static final BigDecimal FULL_REDUCTION_AMOUNT_2 = new BigDecimal("30");
    private static final BigDecimal FULL_REDUCTION_THRESHOLD_3 = new BigDecimal("500");
    private static final BigDecimal FULL_REDUCTION_AMOUNT_3 = new BigDecimal("80");

    /**
     * 下单核心事务（防超卖）：
     * 1. @Transactional 开启 SQL 事务（BEGIN）；
     * 2. 逐本图书 SELECT ... FOR UPDATE 加行锁，锁内的库存校验基于最新值，
     *    并发下单同一本书时在此处串行排队；
     * 3. UPDATE ... SET stock = stock - #{q} WHERE stock >= #{q} 原子扣减，
     *    即使锁失效也不会扣成负数；
     * 4. 任何一步抛异常 → 事务 ROLLBACK，已加的锁随事务结束自动释放。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(Long userId, OrderCreateRequest request) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        boolean directBuy = request.getBookId() != null;
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (directBuy) {
            // 一键直购（开抢快速结算）：绕过购物车，直接锁定单本图书
            int quantity = request.getQuantity() != null && request.getQuantity() > 0
                    ? request.getQuantity() : 1;
            // 无锁库存预判：快速拦截缺货/下架的请求，避免进入 FOR UPDATE 行锁排队
            bookService.preCheckBookStock(request.getBookId(), quantity);
            Book book = bookService.getBookForUpdate(request.getBookId());
            validatePurchasable(book, quantity);
            OrderItem item = buildOrderItem(book, quantity, LocalDateTime.now());
            totalAmount = totalAmount.add(item.getSubtotal());
            orderItems.add(item);
        } else {
            List<Cart> cartItems = cartService.getCartByUserId(userId);
            if (cartItems.isEmpty()) {
                throw new BusinessException("购物车为空");
            }
            // 清理购物车中书名已被删除的失效条目（图书不存在/已被逻辑删除时，
            // 无视它们继续下单，否则会反复提示"图书不存在或已下架"阻塞整个订单）
            List<Cart> invalidItems = cartItems.stream()
                    .filter(cartItem -> !bookService.isBookExisting(cartItem.getBookId()))
                    .collect(Collectors.toList());
            for (Cart invalid : invalidItems) {
                cartService.removeFromCart(userId, invalid.getId());
            }
            cartItems.removeAll(invalidItems);
            if (cartItems.isEmpty()) {
                throw new BusinessException("购物车为空");
            }

            // 【性能优化】无锁库存预判：用普通 SELECT 快速拒绝明显缺货/已下架的请求，
            // 避免它们进入 SELECT...FOR UPDATE 行锁排队，降低锁竞争和事务等待时间。
            // 该检查基于可能略旧的快值，少量边界 case 由后续 FOR UPDATE + 原子扣减兜底，
            // 不会影响正确性（不会超卖），仅减少无谓锁等待。
            for (Cart cartItem : cartItems) {
                bookService.preCheckBookStock(cartItem.getBookId(), cartItem.getQuantity());
            }

            for (Cart cartItem : cartItems) {
                Book book = bookService.getBookForUpdate(cartItem.getBookId());
                validatePurchasable(book, cartItem.getQuantity());
                OrderItem item = buildOrderItem(book, cartItem.getQuantity(), LocalDateTime.now());
                totalAmount = totalAmount.add(item.getSubtotal());
                orderItems.add(item);
            }
        }

        BigDecimal memberDiscount = user.getMemberDiscount() != null ? user.getMemberDiscount() : BigDecimal.ONE;
        BigDecimal afterMemberDiscount = totalAmount.multiply(memberDiscount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fullReductionAmount = calculateFullReduction(afterMemberDiscount);
        BigDecimal discountAmount = totalAmount.subtract(afterMemberDiscount).add(fullReductionAmount);
        BigDecimal payAmount = afterMemberDiscount.subtract(fullReductionAmount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }

        String orderNo = generateOrderNo();

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(payAmount);
        order.setMemberDiscount(memberDiscount);
        order.setFullReductionAmount(fullReductionAmount);
        order.setStatus(0);
        order.setAddress(request.getAddress());
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setRemark(request.getRemark());
        this.save(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
        }
        orderItemService.saveBatch(orderItems);

        // 原子扣库存 + 累加销量：受影响行数为 0 即库存不足，抛异常触发整体回滚
        for (OrderItem item : orderItems) {
            bookService.deductStock(item.getBookId(), item.getQuantity());
        }

        if (!directBuy) {
            cartService.clearCart(userId);
        }

        OrderVO vo = convertToVO(order);
        vo.setItems(convertItemVOs(orderItems));
        return vo;
    }

    /**
     * 抢购资格校验（在 FOR UPDATE 行锁内读取的是最新库存）：
     * 并发场景下后到的请求会在这里拿到已扣减后的真实库存并收到明确的"未抢到"提示。
     */
    private void validatePurchasable(Book book, int quantity) {
        if (book == null || book.getStatus() != 1) {
            throw new BusinessException("图书「" + (book != null ? book.getTitle() : "") + "」已下架");
        }
        // 未开抢：开抢时间在未来，禁止下单（前端以倒计时引导，此处为后端兜底）
        if (bookService.isGrabNotStarted(book)) {
            throw new BusinessException(
                    "图书「" + book.getTitle() + "」尚未开抢，暂不可下单，请等待开抢倒计时结束");
        }
        if (book.getStock() <= 0) {
            throw new BusinessException(STOCK_CONFLICT_CODE,
                    "手慢了！图书「" + book.getTitle() + "」已被抢光，未获得本次购买资格");
        }
        if (book.getStock() < quantity) {
            throw new BusinessException(STOCK_CONFLICT_CODE,
                    "图书「" + book.getTitle() + "」仅剩 " + book.getStock()
                            + " 件，不足您要购买的数量 " + quantity + "，未抢到");
        }
    }

    /**
     * 计算单本图书的成交价/小计并构建订单明细（与后端下单口径一致的价格快照）
     */
    private OrderItem buildOrderItem(Book book, int quantity, LocalDateTime now) {
        boolean onPromo = book.getPromoStartTime() != null && book.getPromoEndTime() != null
                && now.isAfter(book.getPromoStartTime()) && now.isBefore(book.getPromoEndTime())
                && book.getPromoPrice() != null;
        BigDecimal unitPrice = book.getPrice();
        BigDecimal discount = book.getDiscount();
        BigDecimal promoPrice = onPromo ? book.getPromoPrice() : null;
        BigDecimal effectivePrice = onPromo ? promoPrice : unitPrice.multiply(discount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal subtotal = effectivePrice.multiply(BigDecimal.valueOf(quantity));

        OrderItem item = new OrderItem();
        item.setBookId(book.getId());
        item.setBookTitle(book.getTitle());
        item.setBookCover(book.getCoverImage());
        item.setPrice(unitPrice);
        item.setDiscount(discount);
        item.setPromoPrice(promoPrice);
        item.setQuantity(quantity);
        item.setSubtotal(subtotal);
        return item;
    }

    @Override
    public List<DeliveryInfo> getRecentAddresses(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .select(Order::getAddress, Order::getReceiverName, Order::getReceiverPhone)
                .orderByDesc(Order::getCreateTime)
                .last("LIMIT 30");
        List<Order> orders = this.list(wrapper);
        List<DeliveryInfo> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Order order : orders) {
            if (order.getAddress() == null || order.getReceiverName() == null || order.getReceiverPhone() == null) {
                continue;
            }
            if (order.getAddress().isBlank() || order.getReceiverName().isBlank() || order.getReceiverPhone().isBlank()) {
                continue;
            }
            String key = order.getReceiverName() + "|" + order.getReceiverPhone() + "|" + order.getAddress();
            if (seen.add(key)) {
                result.add(new DeliveryInfo(order.getReceiverName(), order.getReceiverPhone(), order.getAddress()));
                if (result.size() >= 6) {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public OrderVO getOrderDetail(Long userId, Long orderId) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        OrderVO vo = convertToVO(order);
        List<OrderVO.OrderItemVO> items =
                loadItemVOs(Collections.singletonList(order.getId())).getOrDefault(orderId, Collections.emptyList());
        vo.setItems(items);
        applyReturnInfo(vo);
        return vo;
    }

    @Override
    public Page<OrderVO> getUserOrders(Long userId, Page<Order> page, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> orderPage = this.page(page, wrapper);
        return convertToVOPage(orderPage);
    }

    @Override
    public Page<OrderVO> getAllOrders(Page<Order> page, Integer status, String keyword) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            if (status == STATUS_RETURN_PENDING) {
                // 待退货审核 = 存在待审核(status=0)退货申请的订单（退货为派生状态，无独立订单状态码）
                wrapper.apply("EXISTS (SELECT 1 FROM return_record rr " +
                        "WHERE rr.order_id = `order`.id AND rr.deleted = 0 AND rr.status = 0)");
            } else {
                wrapper.eq(Order::getStatus, status);
            }
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Order::getOrderNo, keyword)
                    .or().like(Order::getReceiverName, keyword));
        }
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> orderPage = this.page(page, wrapper);
        return convertToVOPage(orderPage);
    }

    /**
     * 支付防线：下单时已锁定并扣减库存，正常情况下必然可付。
     * 但若商品在待支付期间被管理员下架/删除，这里拒绝支付并明确告知原因，
     * 避免用户对无法履约的订单付款（配合前端 409 提示"商品已下架"）。
     */
    @Override
    public void payOrder(Long userId, Long orderId) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不正确，无法支付");
        }

        // 支付校验失败（图书管理员下架/未开抢）：自动取消该未支付订单并回补库存，
        // 避免"未抢到/无法履约"的订单残留为未支付状态堆积在后台。
        // 特例：图书因"抢购到最后一本"被自动下架（status=0 且库存<=0）时，
        // 该订单已锁定库存，赢家应正常支付成功，仅拦截后续买家。
        List<OrderItem> items = loadItems(Collections.singletonList(orderId));
        for (OrderItem item : items) {
            Book book = bookService.getById(item.getBookId());
            if (book == null) {
                autoCancelUnpaidOrder(order, items);
                throw new BusinessException(STOCK_CONFLICT_CODE,
                        "订单中的图书「" + item.getBookTitle() + "」已被删除，支付失败，订单已自动取消");
            }
            boolean autoOffShelfBySoldOut = book.getStatus() != 1
                    && book.getStock() != null && book.getStock() <= 0;
            if (book.getStatus() != 1 && !autoOffShelfBySoldOut) {
                autoCancelUnpaidOrder(order, items);
                throw new BusinessException(STOCK_CONFLICT_CODE,
                        "订单中的图书「" + item.getBookTitle() + "」已下架，支付失败，订单已自动取消");
            }
            if (bookService.isGrabNotStarted(book)) {
                autoCancelUnpaidOrder(order, items);
                throw new BusinessException(STOCK_CONFLICT_CODE,
                        "订单中的图书「" + item.getBookTitle() + "」尚未开抢，支付失败，订单已自动取消");
            }
        }

        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        this.updateById(order);
    }

    /**
     * 自动回滚未支付订单：回补已扣库存，并将订单置为已取消(4)。
     * 供支付失败路径调用，保证不残留"未支付"订单。
     */
    private void autoCancelUnpaidOrder(Order order, List<OrderItem> items) {
        for (OrderItem item : items) {
            try {
                bookService.restoreStock(item.getBookId(), item.getQuantity());
            } catch (Exception ignored) {
                // 回补库存失败不阻断订单取消
            }
        }
        order.setStatus(4);
        order.setReturnTime(LocalDateTime.now());
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1 && order.getStatus() != 6) {
            throw new BusinessException("订单状态不正确，无法发货");
        }
        order.setStatus(2);
        order.setShipTime(LocalDateTime.now());
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveOrder(Long userId, Long orderId) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("订单状态不正确，无法确认收货");
        }
        order.setStatus(3);
        order.setReceiveTime(LocalDateTime.now());
        this.updateById(order);
    }

    /**
     * 取消待支付订单：回补库存（原子 UPDATE），与状态变更同处一个事务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只能取消待支付的订单");
        }

        List<OrderItem> items = loadItems(Collections.singletonList(orderId));
        for (OrderItem item : items) {
            bookService.restoreStock(item.getBookId(), item.getQuantity());
        }

        order.setStatus(4);
        order.setReturnTime(LocalDateTime.now());
        this.updateById(order);
    }

    @Override
    public void updateStatus(Long orderId, Integer status) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setStatus(status);
        this.updateById(order);
    }

    private BigDecimal calculateFullReduction(BigDecimal amount) {
        if (amount.compareTo(FULL_REDUCTION_THRESHOLD_3) >= 0) {
            return FULL_REDUCTION_AMOUNT_3;
        } else if (amount.compareTo(FULL_REDUCTION_THRESHOLD_2) >= 0) {
            return FULL_REDUCTION_AMOUNT_2;
        } else if (amount.compareTo(FULL_REDUCTION_THRESHOLD_1) >= 0) {
            return FULL_REDUCTION_AMOUNT_1;
        }
        return BigDecimal.ZERO;
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));
    }

    /**
     * 批量查询订单明细并按 orderId 分组，避免逐单查询的 N+1 问题
     */
    private Map<Long, List<OrderVO.OrderItemVO>> loadItemVOs(List<Long> orderIds) {
        List<OrderItem> items = orderItemService.list(new LambdaQueryWrapper<OrderItem>()
                .in(OrderItem::getOrderId, orderIds));
        return items.stream()
                .map(this::convertItemToVO)
                .collect(Collectors.groupingBy(OrderVO.OrderItemVO::getOrderId));
    }

    private Page<OrderVO> convertToVOPage(Page<Order> orderPage) {
        Page<OrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<Long> orderIds = orderPage.getRecords().stream().map(Order::getId).collect(Collectors.toList());
        Map<Long, List<OrderVO.OrderItemVO>> itemsByOrderId =
                orderIds.isEmpty() ? Collections.emptyMap() : loadItemVOs(orderIds);

        List<OrderVO> voList = orderPage.getRecords().stream()
                .map(order -> {
                    OrderVO vo = convertToVO(order);
                    vo.setItems(itemsByOrderId.getOrDefault(order.getId(), Collections.emptyList()));
                    applyReturnInfo(vo);
                    return vo;
                })
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 基于 return_record 计算订单的派生退货状态（returnState）与各明细的已退/可退数量。
     * 退货不再用订单 binary 状态 4/5/6 标记，而是实时派生于退货记录，支持部分退货。
     *
     * 约定：
     * - returnedQty   = 已退数量（待审核 + 已通过），用于"还能退几件"的展示
     * - fullyReturned = 仅依据"审核通过(status=1)"的数量判定，未通过的待审核不算已退
     */
    private void applyReturnInfo(OrderVO vo) {
        List<Map<String, Object>> returnedRows = returnRecordMapper.selectReturnedQtyByOrder(vo.getId());
        if (returnedRows == null || returnedRows.isEmpty()) {
            vo.setReturnState(0);
            vo.setReturnStateText("无退货");
            vo.getItems().forEach(it -> {
                it.setReturnedQty(0);
                it.setReturnableQty(it.getQuantity() == null ? 0 : it.getQuantity());
            });
            return;
        }

        // 已退数量（待审核+已通过）
        Map<Long, Integer> returnedByItem = new HashMap<>();
        for (Map<String, Object> row : returnedRows) {
            returnedByItem.put(((Number) row.get("order_item_id")).longValue(),
                    ((Number) row.get("returnedQty")).intValue());
        }
        // 已审核通过的数量（用于判定是否"已退完"）
        Map<Long, Integer> approvedByItem = new HashMap<>();
        for (Map<String, Object> row : returnRecordMapper.selectApprovedQtyByOrder(vo.getId())) {
            approvedByItem.put(((Number) row.get("order_item_id")).longValue(),
                    ((Number) row.get("approvedQty")).intValue());
        }

        boolean hasPending = returnRecordMapper.countPendingByOrderId(vo.getId()) > 0;
        boolean anyApproved = approvedByItem.values().stream().anyMatch(q -> q > 0);

        boolean fullyReturned = !vo.getItems().isEmpty();
        for (OrderVO.OrderItemVO item : vo.getItems()) {
            int returned = returnedByItem.getOrDefault(item.getId(), 0);
            item.setReturnedQty(returned);
            item.setReturnableQty(Math.max(0, item.getQuantity() - returned));
            int approved = approvedByItem.getOrDefault(item.getId(), 0);
            if (approved < item.getQuantity()) {
                fullyReturned = false;
            }
        }

        if (fullyReturned) {
            vo.setReturnState(3);
            vo.setReturnStateText("已退货");
        } else if (hasPending) {
            vo.setReturnState(1);
            vo.setReturnStateText("退货审核中");
        } else if (anyApproved) {
            vo.setReturnState(2);
            vo.setReturnStateText("部分退货");
        } else {
            vo.setReturnState(0);
            vo.setReturnStateText("无退货");
        }
    }

    private List<OrderItem> loadItems(List<Long> orderIds) {
        return orderItemService.list(new LambdaQueryWrapper<OrderItem>()
                .in(OrderItem::getOrderId, orderIds));
    }

    private List<OrderVO.OrderItemVO> convertItemVOs(List<OrderItem> items) {
        return items.stream().map(this::convertItemToVO).collect(Collectors.toList());
    }

    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setMemberDiscount(order.getMemberDiscount());
        vo.setFullReductionAmount(order.getFullReductionAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusText(getStatusText(order.getStatus()));
        vo.setPayTime(order.getPayTime());
        vo.setShipTime(order.getShipTime());
        vo.setReceiveTime(order.getReceiveTime());
        vo.setReturnTime(order.getReturnTime());
        vo.setAddress(order.getAddress());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }

    private OrderVO.OrderItemVO convertItemToVO(OrderItem item) {
        OrderVO.OrderItemVO vo = new OrderVO.OrderItemVO();
        vo.setId(item.getId());
        vo.setOrderId(item.getOrderId());
        vo.setBookId(item.getBookId());
        vo.setBookTitle(item.getBookTitle());
        vo.setBookCover(item.getBookCover());
        vo.setPrice(item.getPrice());
        vo.setDiscount(item.getDiscount());
        vo.setPromoPrice(item.getPromoPrice());
        vo.setQuantity(item.getQuantity());
        vo.setSubtotal(item.getSubtotal());
        return vo;
    }

    private String getStatusText(Integer status) {
        return switch (status) {
            case 0 -> "未付款";
            case 1 -> "未发货";
            case 2 -> "已发货";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知";
        };
    }
}
