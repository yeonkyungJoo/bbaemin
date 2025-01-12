package org.bbaemin.api.order.controller.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bbaemin.api.order.enums.PaymentMethod;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateOrderRequest {

    private String deliveryAddress;                 // 배달주소
    private String phoneNumber;                     // 전화번호
    private String email;                           // 주문 내역 발송 메일
    private String messageToRider;                  // 라이더님께

//    private boolean reserve;                      // 바로 배달 vs 예약 배달
    // TODO - 품절 상품만 취소 vs 전체 주문 취소

    private List<Long> discountCouponIdList;        // 할인쿠폰 - 중복 사용

    private PaymentMethod paymentMethod;            // 결제 수단

    @Builder
    private CreateOrderRequest(String deliveryAddress, String phoneNumber, String email, String messageToRider, List<Long> discountCouponIdList, PaymentMethod paymentMethod) {
        this.deliveryAddress = deliveryAddress;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.messageToRider = messageToRider;
        this.discountCouponIdList = discountCouponIdList;
        this.paymentMethod = paymentMethod;
    }
}
