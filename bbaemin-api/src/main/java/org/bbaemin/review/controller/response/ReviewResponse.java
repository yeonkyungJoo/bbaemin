package org.bbaemin.review.controller.response;

import org.bbaemin.review.vo.Review;

public class ReviewResponse {

    private int score;
    private String content;
    private String image;


    public ReviewResponse(Review review) {
        this.score = review.getScore();
        this.content = review.getContent();
        this.image = review.getImage();
    }
}
