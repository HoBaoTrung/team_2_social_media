package com.codegym.socialmedia.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostReactionSummaryDto {
    private Long postId;
    private long totalReactions;
    private Map<String, Long> reactionCounts; // "LIKE": 15, "LOVE": 3, "HAHA": 2
    private List<PostReactionDto> recentReactions;
    private boolean currentUserReacted;
    private String currentUserReactionType;
}