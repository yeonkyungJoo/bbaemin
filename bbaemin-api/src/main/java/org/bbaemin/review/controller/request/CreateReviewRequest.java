package org.bbaemin.review.controller.request;

import lombok.Getter;

@Getter
public class CreateReviewRequest {

    private Long orderId;

    private int score;
    private String content;
    private String image;
}
