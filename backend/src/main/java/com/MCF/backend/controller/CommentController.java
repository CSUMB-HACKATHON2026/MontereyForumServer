package com.MCF.backend.controller;

import com.MCF.backend.dto.request.CreateCommentRequest;
import com.MCF.backend.dto.request.UpdateCommentRequest;
import com.MCF.backend.dto.response.CommentResponse;
import com.MCF.backend.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/issues/{issueId}/comments")
    public List<CommentResponse> getCommentsByIssue(@PathVariable Long issueId) {
        return commentService.getCommentsByIssue(issueId);
    }

    @PostMapping("/issues/{issueId}/comments")
    public CommentResponse createComment(@PathVariable Long issueId,
                                         @RequestBody CreateCommentRequest request) {
        return commentService.createComment(issueId, request);
    }

    @PutMapping("/comments/{commentId}")
    public CommentResponse updateComment(@PathVariable Long commentId,
                                         @RequestBody UpdateCommentRequest request) {
        return commentService.updateComment(commentId, request);
    }

    @DeleteMapping("/comments/{commentId}")
    public void deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
    }
}