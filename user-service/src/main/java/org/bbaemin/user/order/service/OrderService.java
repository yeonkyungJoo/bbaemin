package org.bbaemin.user.order.service;

import lombok.RequiredArgsConstructor;
import org.bbaemin.dto.request.ApplyCouponRequest;
import org.bbaemin.user.cart.service.CartItemService;
import org.bbaemin.user.cart.vo.CartItem;
import org.bbaemin.user.order.enums.OrderStatus;
import org.bbaemin.user.order.repository.OrderItemRepository;
import org.bbaemin.user.order.repository.OrderRepository;
import org.bbaemin.user.order.vo.Order;
import org.bbaemin.user.order.vo.OrderItem;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.NoSuchElementException;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemService cartItemService;
    private final DeliveryFeeService deliveryFeeService;

    private WebClient client = WebClient.create();

    public Flux<Order> getOrderListByUserId(Long userId) {
        return orderRepository.findByUserId(userId)
                .log();
    }

    public Mono<Order> getOrder(Long userId, Long orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("orderId : " + orderId)))
                .log();
    }

    public Flux<OrderItem> getOrderItemListByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId)
                .log();
    }

    public Mono<OrderItem> getOrderItem(Long orderItemId) {
        return orderItemRepository.findById(orderItemId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("orderItemId : " + orderItemId)))
                .log();
    }

    public Mono<Integer> applyCouponList(int orderAmount, List<Long> discountCouponIdList) {
        // publisher
        return client.post()
                // TODO - property
                .uri("http://localhost:8080/api/v1/coupons/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ApplyCouponRequest(orderAmount, discountCouponIdList))
                .retrieve()
                .bodyToMono(Integer.class)
                .log();
    }

    @Transactional
    public Mono<Order> order(Long userId, Order order, List<Long> discountCouponIdList) {

        Flux<CartItem> cartItemFlux = cartItemService.getCartItemListByUserId(userId);
//                .log();
        Flux<OrderItem> orderItemFlux = cartItemFlux
                .map(cartItem -> OrderItem.builder()
                        .itemId(cartItem.getItemId())
                        .itemName(cartItem.getItemName())
                        .itemDescription(cartItem.getItemDescription())
                        .orderPrice(cartItem.getOrderPrice())
                        .orderCount(cartItem.getOrderCount())
                        .build());
//                .log();
        
        // 주문금액
        Mono<Integer> orderAmount = cartItemFlux
                .map(cartItem -> cartItem.getOrderPrice() * cartItem.getOrderCount())
                .reduce(Integer::sum);
//                .log();
        // 배달비
        Mono<Integer> deliveryFee = orderAmount
                .flatMap(deliveryFeeService::getDeliveryFee);
//                .log();
        // 할인금액
        Mono<Integer> totalDiscountAmount = orderAmount
                .flatMap(amount -> applyCouponList(amount, discountCouponIdList));
//                .log();
        // 총 금액
        Mono<Integer> paymentAmount = orderAmount
                .zipWith(deliveryFee, Integer::sum)
                .zipWith(totalDiscountAmount, (sum, discount) -> sum - discount);
//                .log();

        Mono<Order> orderMono = Mono.zip(Mono.just(order), orderAmount, deliveryFee, paymentAmount)
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
                    return o;
                })
                .flatMap(orderRepository::save);
//                .log();

        // TODO - TEST
///////////////////////////////////// TEST /////////////////////////////////////
        orderMono
                .flatMapMany(o -> orderItemFlux
                        .map(orderItem -> {
                            orderItem.setOrderId(o.getOrderId());
                            return orderItem;
                        }))
                .doOnNext(orderItemRepository::save)
                .doAfterTerminate(() -> cartItemService.clear(userId))
                .log();
///////////////////////////////////////////////////////////////////////////////
        return orderMono;
    }

    @Transactional
    public Mono<Void> deleteOrder(Long userId, Long orderId) {
        return orderItemRepository.deleteByOrderId(orderId)
                .thenEmpty(orderRepository.deleteById(orderId))
                .log();
    }

    @Transactional
    public Mono<Order> cancelOrder(Long userId, Long orderId) {
        return getOrder(userId, orderId)
                .map(order -> {
                    order.setStatus(OrderStatus.CANCEL_ORDER);
                    return order;
                })
                .flatMap(orderRepository::save)
                .log();
    }
}
