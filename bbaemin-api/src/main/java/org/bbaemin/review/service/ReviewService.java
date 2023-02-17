package org.bbaemin.review.service;

import lombok.RequiredArgsConstructor;
import org.bbaemin.review.repository.ReviewRepository;
import org.bbaemin.review.vo.Review;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public List<Review> getReviewList() {
        return null;
    }

    public Review getReview(Long reviewId) {
        return null;
    }

    public Review createReview(Review review) {
        return null;
    }

    public Review updateReview(Review review) {
        return null;
    }

    public void deleteReview(Long reviewId) {

    }
}
