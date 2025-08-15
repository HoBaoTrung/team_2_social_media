package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.social_action.LikeStatus;
import com.codegym.socialmedia.model.social_action.LikeStatusId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeStatusRepository extends JpaRepository<LikeStatus, LikeStatusId> {
    int countByStatusId(Integer userId);
}
