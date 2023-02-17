package org.bbaemin.review.controller;

import lombok.RequiredArgsConstructor;
import org.bbaemin.config.response.ApiResult;
import org.bbaemin.review.controller.request.CreateReviewRequest;
import org.bbaemin.review.controller.request.UpdateReviewRequest;
import org.bbaemin.review.controller.response.ReviewResponse;
import org.bbaemin.review.service.ReviewService;
import org.bbaemin.review.vo.Review;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 리스트
    @GetMapping
    public ApiResult<List<ReviewResponse>> listReview() {
        List<ReviewResponse> reviewList = reviewService.getReviewList().stream()
                .map(ReviewResponse::new).collect(Collectors.toList());
        return ApiResult.ok(reviewList);
    }

    // 리뷰 조회
    @GetMapping("/{reviewId}")
    public ApiResult<ReviewResponse> getReview(@PathVariable Long reviewId) {
        Review review = reviewService.getReview(reviewId);
        return ApiResult.ok(new ReviewResponse(review));
    }

    // 리뷰 등록
    @PostMapping
    public ApiResult<ReviewResponse> createReview(@RequestBody CreateReviewRequest createReviewRequest) {
//        Review review = Review.builder()
//                .orderId()
//                .build();
//        reviewService.createReview();
        return null;
    }

    // 리뷰 수정
    @PutMapping("/{reviewId}")
    public ApiResult<ReviewResponse> updateReview(@PathVariable Long reviewId, @RequestBody UpdateReviewRequest updateReviewRequest) {
        return null;
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ApiResult<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ApiResult.ok();
    }
}
