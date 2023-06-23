package org.bbaemin.api.order.service;

import lombok.RequiredArgsConstructor;
import org.bbaemin.api.cart.controller.response.CartItemResponse;
import org.bbaemin.api.cart.controller.response.CartResponse;
import org.bbaemin.api.cart.service.CartItemService;
import org.bbaemin.api.cart.service.DeliveryFeeService;
import org.bbaemin.api.cart.vo.CartItem;
import org.bbaemin.api.item.controller.response.ItemResponse;
import org.bbaemin.api.order.enums.OrderStatus;
import org.bbaemin.api.order.repository.OrderItemRepository;
import org.bbaemin.api.order.repository.OrderRepository;
import org.bbaemin.api.order.vo.Order;
import org.bbaemin.api.order.vo.OrderItem;
import org.bbaemin.api.user.service.UserService;
import org.bbaemin.api.user.vo.User;
import org.bbaemin.config.response.ApiResult;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemService cartItemService;
    private final DeliveryFeeService deliveryFeeService;
    private final UserService userService;
    private final CouponService couponService;

    private final RestTemplate restTemplate;

    private String admin = "localhost:8080";
    private String user = "localhost:8081";

    public List<Order> getOrderListByUserId(Long userId) {
        User user = userService.getUser(userId);
        return orderRepository.findByUser(user);
    }

    public Order getOrder(Long userId, Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("orderId : " + orderId));
    }

    public List<OrderItem> getOrderItemListByOrder(Order order) {
        return orderItemRepository.findByOrder(order);
    }

    public OrderItem getOrderItem(Long orderItemId) {
        return orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NoSuchElementException("orderItemId : " + orderItemId));
    }

    private <T> ResponseEntity<T> get(URI url, ParameterizedTypeReference<T> responseType) {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null, headers), responseType);
    }

    private <T> ResponseEntity<T> post(URI url, Object bodyValue, ParameterizedTypeReference<T> responseType) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(bodyValue, headers);

        return restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
    }

    private <T> ResponseEntity<T> patch(URI url, Object bodyValue, ParameterizedTypeReference<T> responseType) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(bodyValue, headers);

        return restTemplate.exchange(url, HttpMethod.PATCH, entity, responseType);
    }

    // 장바구니 조회
    ResponseEntity<ApiResult<CartResponse>> getCart(Long userId) {
        URI uri = new DefaultUriBuilderFactory(user).builder().path(CART_SERVICE.getPath()).queryParam("userId", userId).build();
        return get(uri, new ParameterizedTypeReference<>() {});
    }

    // 재고 조회 및 재고 차감 처리
    ResponseEntity<ApiResult<ItemResponse>> deductItem(Long itemId, int orderCount) {
        URI uri = new DefaultUriBuilderFactory(admin).builder().path(ITEM_SERVICE.getPath()).path("/{itemId}/deduct").build(itemId);
        return patch(uri, new DeductItemRequest(itemId, orderCount), new ParameterizedTypeReference<>() {});
    }

    // 재고 복구
    ResponseEntity<ApiResult<ItemResponse>> restoreItem(Long itemId, int orderCount) {
        URI uri = new DefaultUriBuilderFactory(admin).builder().path(ITEM_SERVICE.getPath()).path("/{itemId}/restore").build(itemId);
        return patch(uri, new RestoreItemRequest(itemId, orderCount), new ParameterizedTypeReference<>() {});
    }

    // 결제
    ResponseEntity<ApiResult<PaymentResponse>> pay(Long providerId, Integer amount, String referenceNumber, String account) {
        URI uri = new DefaultUriBuilderFactory(admin).builder().path(PAYMENT_SERVICE.getPath()).path("/process").build();
        return post(uri, new PayRequest(providerId, amount, referenceNumber, account), new ParameterizedTypeReference<>() {});
    }

    // 결제 취소
    ResponseEntity<ApiResult<PaymentResponse>> cancelPay(Long orderId) {
        URI uri = new DefaultUriBuilderFactory(admin).builder().path(PAYMENT_SERVICE.getPath()).path("/cancel").build();
        return post(uri, new CancelPayRequest(orderId), new ParameterizedTypeReference<>() {});
    }

    // 쿠폰 적용
    ResponseEntity<ApiResult<Integer>> applyCouponList(int orderAmount, List<Long> discountCouponIdList) {
        URI uri = new DefaultUriBuilderFactory(admin).builder().path(COUPON_SERVICE.getPath()).path("/apply").build();
        return patch(uri, new ApplyCouponRequest(orderAmount, discountCouponIdList), new ParameterizedTypeReference<>() {});
    }

    // 이메일 전송
    ResponseEntity<ApiResult<Void>> sendEmail(String userEmail, String content) {
        URI uri = new DefaultUriBuilderFactory(admin).builder().path(EMAIL_SERVICE.getPath()).path("/send").build();
        return post(uri, new SendEmailRequest(userEmail, content), new ParameterizedTypeReference<>() {});
    }

    // 배달 정보 전송
    ResponseEntity<ApiResult<Void>> sendDeliveryInfo(String deliveryAddress, Long orderId) {
        URI uri = new DefaultUriBuilderFactory(admin).builder().path(DELIVERY_SERVICE.getPath()).path("/send").build();
        return post(uri, new SendDeliveryInfoRequest(deliveryAddress, orderId), new ParameterizedTypeReference<>() {});
    }

    @Transactional
    public Order order(Long userId, Order order, List<Long> discountCouponIdList) {

        User user = userService.getUser(userId);
        order.setUser(user);

        // #1. 장바구니 조회 (CartService와 분리되어 있다고 가정)
//        List<CartItem> cartItemList = cartItemService.getCartItemListByUserId(userId);
        CartResponse cart = this.getCart(userId).getBody().getResult();
        List<CartItemResponse> cartItemList = cart.getCartItemList();

        for (CartItemResponse cartItem : cartItemList) {
            // #2. 재고 조회 및 재고 차감 처리
            // -> 재고 부족 시 주문 불가 처리
            ResponseEntity<ApiResult<ItemResponse>> responseEntity = this.deductItem(cartItem.getItemId(), cartItem.getOrderCount());
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                // throw new OrderException(error);
                this.sendEmail(order.getEmail(), "ORDER FAIL - ITEM FAIL");
            }
        }

        // #3. 금액 조회
        // #3-1. 주문금액
        Integer orderAmount = cartItemList.stream().mapToInt(cartItem -> cartItem.getOrderPrice() * cartItem.getOrderCount()).sum();

        // #3-2. 배달비 조회
        Integer deliveryFee = deliveryFeeService.getDeliveryFee(orderAmount);

        // #3-3. 쿠폰 적용(할인금액 조회)
        Integer totalDiscountAmount = this.applyCouponList(orderAmount, discountCouponIdList).getBody().getResult();

        // TODO - 어떻게 테스트 하나요?
        List<OrderItem> orderItemList = cartItemList.stream()
                .map(cartItem -> OrderItem.builder()
                        .item(cartItem.getItem())
                        .itemName(cartItem.getItemName())
                        .itemDescription(cartItem.getItemDescription())
                        .orderPrice(cartItem.getOrderPrice())
                        .orderCount(cartItem.getOrderCount())
                        .build())
                .collect(Collectors.toList());
        order.setOrderAmount(orderAmount);
        order.setDeliveryFee(deliveryFee);

        int totalDiscountAmount = couponService.apply(orderAmount, discountCouponIdList);
        order.setPaymentAmount(orderAmount + deliveryFee - totalDiscountAmount);

        Order saved = orderRepository.save(order);
        orderItemList.forEach(orderItem -> {
            orderItem.setOrder(saved);
            orderItemRepository.save(orderItem);
        });

        cartItemService.clear(userId);
        return saved;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////


                .doOnComplete(() -> {







                    // #3-4. 총 금액
                    orderAmount
                            .zipWith(deliveryFee, Integer::sum)
                            .zipWith(totalDiscountAmount, (sum, discount) -> sum - discount)
                            .doOnSuccess(paymentAmount ->

                                            // #4. 결제 -> 결제 실패 시 주문 불가 처리
                                            // TODO - 결제 데이터
                                            this.pay(1L, paymentAmount, "referenceNumber", "account")
                                                    .doOnError(throwable -> {
//                                                throw new OrderException(throwable);

                                                        // # 5-1. 주문 실패 메일 전송
                                                        this.sendEmail(order.getEmail(), "ORDER FAIL - PAYMENT FAIL");

                                                        order.setStatus(FAIL_ORDER);
                                                        orderRepository.save(order);
                                                    })
                                                    .doOnSuccess(result -> {

                                                        Long paymentId = result.getResult().getPaymentId();

                                                        // #5-1. (결제까지 완료 시) 주문 생성
                                                        Mono.zip(Mono.just(order), orderAmount, deliveryFee, Mono.just(paymentAmount))
                                                                .map(tuple -> {
                                                                    Order o = tuple.getT1();
                                                                    Integer _orderAmount = tuple.getT2();
                                                                    Integer _deliveryFee = tuple.getT3();
                                                                    Integer _paymentAmount = tuple.getT4();
                                                                    o.setOrderAmount(_orderAmount);
                                                                    o.setDeliveryFee(_deliveryFee);
                                                                    o.setPaymentAmount(_paymentAmount);
                                                                    return o;
                                                                })
                                                                .map(o -> {
                                                                    o.setUserId(userId);
                                                                    o.setPaymentId(paymentId);
                                                                    o.setStatus(COMPLETE_ORDER);
                                                                    return o;
                                                                })
                                                                .flatMap(orderRepository::save)
                                                                .flatMapMany(o -> cartItemFlux.map(cartItem -> OrderItem.builder()
                                                                        .orderId(o.getOrderId())
                                                                        .itemId(cartItem.getItemId())
                                                                        .itemName(cartItem.getItemName())
                                                                        .itemDescription(cartItem.getItemDescription())
                                                                        .orderPrice(cartItem.getOrderPrice())
                                                                        .orderCount(cartItem.getOrderCount())
                                                                        .build()))
                                                                .doOnNext(orderItem -> {
                                                                    order.addOrderItem(orderItem);
                                                                    orderItemRepository.save(orderItem);
                                                                })
                                                                .doOnComplete(() -> {
                                                                    cartItemService.clear(userId);
                                                                })
                                                                .then(
                                                                        // # 6. 주문 성공 메일 전송
                                                                        this.sendEmail(order.getEmail(), "ORDER SUCCESS")
                                                                )
                                                                .then(
                                                                        // # 7. 배달 정보 전송
                                                                        this.sendDeliveryInfo(order.getDeliveryAddress(), order.getOrderId())
                                                                );
                                                    })
                            );
                })
                .then(Mono.just(order));
    }

    @Transactional
    public void deleteOrder(Long userId, Long orderId) {
        Order order = getOrder(userId, orderId);
        orderItemRepository.deleteByOrder(order);
        orderRepository.delete(order);
    }

    @Transactional
    public Order cancelOrder(Long userId, Long orderId) {
        // TODO - vs updateStatusCancel
        Order order = getOrder(userId, orderId);
        order.setStatus(OrderStatus.CANCEL_ORDER);
        return order;
//////////////////////////////////////////////////////////////////////////////////////
        return getOrder(userId, orderId)
                .doOnSuccess(order -> {

                    // #1. 재고 복구
                    List<OrderItem> orderItemList = order.getOrderItemList();
                    Flux.fromIterable(orderItemList)
                            .doOnNext(orderItem -> {
                                this.restoreItem(orderItem.getItemId(), orderItem.getOrderCount());
                            });

                    // #2. 결제 취소
                    this.cancelPay(orderId);

                })
                .map(order -> {
                    order.setStatus(OrderStatus.CANCEL_ORDER);
                    return order;
                })
                // #3. '주문 취소'로 상태 변경
                .flatMap(orderRepository::save)
                .doOnSuccess(order -> {
                    // # 4. 주문 취소 메일 전송
                    this.sendEmail(order.getEmail(), "ORDER CANCEL");
                })
                .doOnSuccess(order -> {
                    // # 5. 배달 취소 정보 전송
                    this.sendDeliveryInfo(order.getDeliveryAddress(), order.getOrderId());
                });
    }
}
