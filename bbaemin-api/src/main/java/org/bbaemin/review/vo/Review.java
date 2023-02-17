package org.bbaemin.review.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Review {

    private Long reviewId;
    private Long orderId;

    private int score;
    private String content;
    private String image;

    @Builder
    private Review(Long orderId, int score, String content, String image) {
        this.orderId = orderId;
        this.score = score;
        this.content = content;
        this.image = image;
    }
}
