package org.bbaemin.order.controller.response;

import lombok.Getter;
import org.bbaemin.order.vo.Order;

@Getter
public class OrderResponse {

    private Long orderId;
    private String status;          // 주문완료, 주문취소 / 배달중, 배달완료, 배달취소
    private String description;
    private String paymentAmount;   // 결제 금액
    private String orderDate;       // 주문일시

    public OrderResponse(Order order) {
        this.orderId = order.getOrderId();
        this.status = order.getStatus();
        this.description = order.getDescription();
        this.paymentAmount = order.getPaymentAmount();
        this.orderDate = order.getOrderDate();
    }
}