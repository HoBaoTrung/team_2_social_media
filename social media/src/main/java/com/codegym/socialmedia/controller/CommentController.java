package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.comment.CommentRequest;
import com.codegym.socialmedia.dto.comment.DisplayCommentDTO;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.PostComment;
import com.codegym.socialmedia.service.post.PostCommentService;
import com.codegym.socialmedia.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    @Autowired
    private final PostCommentService postCommentService;
    @Autowired
    private final UserService userService;

    @PostMapping("/add")
    public DisplayCommentDTO addComment(@RequestBody CommentRequest req) {
        PostComment saved = postCommentService.addComment(req.getPostId(), userService.getCurrentUser(), req.getContent());
        return new DisplayCommentDTO(saved, userService.getCurrentUser(), false);
    }

    @GetMapping("/{postId}")
    public Page<DisplayCommentDTO> getComments(@PathVariable Long postId,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return postCommentService.getCommentsByPost(postId, userService.getCurrentUser(), page, size);
    }

    @PutMapping("/{id}")
    public DisplayCommentDTO editComment(@RequestBody CommentRequest req, @PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        PostComment updated = postCommentService.updateComment(id, currentUser, req.getContent());
        return postCommentService.mapToDTO(updated, currentUser); // trả về DTO với quyền
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();

        Optional<PostComment> deletedComment = postCommentService.DeleteCommentAndReturn(id, currentUser);
        if (deletedComment.isPresent()) {
            // Trả về DTO
            DisplayCommentDTO dto = new DisplayCommentDTO(deletedComment.get(), currentUser, false);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "deletedComment", dto
            ));
        } else {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Bạn không có quyền xóa bình luận này"
            ));
        }
    }

}


