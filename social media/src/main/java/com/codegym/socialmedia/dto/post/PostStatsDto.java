package com.codegym.socialmedia.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostStatsDto {
    private Long postId;
    private long viewCount;
    private long likeCount;
    private long commentCount;
    private long shareCount;
    private Map<String, Long> reactionCounts; // LIKE: 10, LOVE: 5, etc.
    private double engagementRate;
}