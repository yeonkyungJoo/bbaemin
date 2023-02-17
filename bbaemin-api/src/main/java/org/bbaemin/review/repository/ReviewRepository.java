package org.bbaemin.review.repository;

import org.bbaemin.review.vo.Review;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ReviewRepository {

    private final Map<Long, Review> map = new ConcurrentHashMap<>();
    private final AtomicLong id = new AtomicLong(0L);

    public List<Review> findAll() {
        return new ArrayList<>(map.values());
    }

    public Review findById(Long reviewId) {
        return map.get(reviewId);
    }

    public Review insert(Review review) {
        Long reviewId = id.incrementAndGet();
        return null;
    }

    public Review update() {
        return null;
    }

    public void delete() {

    }
}
