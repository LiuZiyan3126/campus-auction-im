package com.ca.module.user.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserProfileVO {
    private UserVO userInfo;
    private Long tradeCount;
    private Double goodRate;
    private List<ReviewItem> recentReviews;

    @Data
    @Builder
    public static class ReviewItem {
        private Long id;
        private String reviewerName;
        private String reviewerAvatar;
        private Integer rating;
        private String content;
        private String createdAt;
    }
}
