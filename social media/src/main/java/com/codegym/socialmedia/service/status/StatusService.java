package com.codegym.socialmedia.service.status;

import com.codegym.socialmedia.dto.status.StatusDTO;
import com.codegym.socialmedia.model.social_action.Status;

import java.util.List;

public interface StatusService {
    List<StatusDTO> getFeeds();

//    void likeStatus(StatusDTO statusDTO);
    boolean toggleLikeStatus(Integer statusId, Long userId);
    //    long getPostCount(Integer userId);
    int getLikeCount(Integer userId);
}
