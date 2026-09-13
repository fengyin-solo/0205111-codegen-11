package com.redtourism.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.redtourism.entity.OrderInfo;

public interface OrderService extends IService<OrderInfo> {
    OrderInfo createOrder(OrderInfo order);
    boolean cancelOrder(Long orderId, Long userId);
    boolean payOrder(Long orderId, String payMethod, Long userId);
    boolean refundOrder(Long orderId, Long userId);
    IPage<OrderInfo> listUserOrders(int page, int size, Long userId, String orderType, String status);
    IPage<OrderInfo> listAllOrders(int page, int size, String orderType, String status);
}
