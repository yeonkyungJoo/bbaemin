package org.bbaemin.order.repository;

import org.bbaemin.order.controller.response.OrderDetailResponse;
import org.bbaemin.order.controller.response.OrderItemResponse;
import org.bbaemin.order.vo.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderMemoryRepository implements OrderRepository {

    private static final Map<Long, Order> map = new ConcurrentHashMap<>();
    private static Long id = 0L;

    static {
        map.put(++id, Order.builder()
                .status("주문이 완료되었어요")
                .store("B마트 강동천호")
                .description("서울우유 저지방우유 1000ml 외 3개 30,340원")
                .orderDate("2022년 11월 13일 오후 8:20")
                .orderId("40024243")
                .orderItemList(Arrays.asList(
                        OrderItemResponse.builder()
                                .itemName("서울우유 저지방우유 1000ml")
                                .orderPrice("3,290원")
                                .orderCount(1)
                                .totalOrderPrice("3,290원")
                                .itemDescription("서울우유 저지방우유 1000ml 1개 (3,290원)")
                                .build(),
                        OrderItemResponse.builder()
                                .itemName("빙그레 딸기맛우유 240ml 4입")
                                .orderPrice("4,990원")
                                .orderCount(1)
                                .totalOrderPrice("4,990원")
                                .itemDescription("빙그레 딸기맛우유 240ml 4입 1개 (4,990원)")
                                .build(),
                        OrderItemResponse.builder()
                                .itemName("[4개 묶음] 제주삼다수 500ml")
                                .orderPrice("790원")
                                .orderCount(4)
                                .totalOrderPrice("3,160원")
                                .itemDescription("제주삼다수 500ml 4개 (790원)")
                                .build(),
                        OrderItemResponse.builder()
                                .itemName("퍼실 2.7L 용기")
                                .orderPrice("18,900원")
                                .orderCount(1)
                                .totalOrderPrice("18,900원")
                                .itemDescription("퍼실 2.7L 용기 1개 (18,900원)")
                                .build()
                ))
                .orderAmount("30,340원")
                .deliveryFee("0원")
                .paymentAmount("30,340원")
                .paymentMethod("신용/체크카드")
                .deliveryAddress("서울시 강동구")
                .phoneNumber("010-1234-5678")
                .email("user@email.com")
                .messageToRider("감사합니다")
                .build());
/*
        map.put(++id, Order.builder()
                .orderId(id)
                .orderDate("2022/11/13(일)")
                .status("주문완료")
                .description("서울우유 저지방우유 1000ml 외 3개")
                .paymentAmount("30,340원")
                .build());
        map.put(++id, Order.builder()
                .orderId(id)
                .orderDate("2022/09/28(수)")
                .status("배달완료")
                .description("아이시스 1L 외 2개")
                .paymentAmount("13,880원")
                .build());
        map.put(++id, Order.builder()
                .orderId(id)
                .orderDate("2022/05/02(월)")
                .status("주문취소")
                .description("빙그레 바나나맛우유 240ml 외 3개")
                .paymentAmount("8,360원")
                .build());*/
    }
}