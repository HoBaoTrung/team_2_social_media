package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.social_action.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
