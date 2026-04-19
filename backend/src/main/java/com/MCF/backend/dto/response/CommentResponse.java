package com.MCF.backend.dto.response;

import com.MCF.backend.model.Comment;

import java.time.LocalDateTime;

public class CommentResponse {
    private Long commentId;
    private Long issueId;
    private Long userId;
    private String username;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentResponse() {
    }

    public CommentResponse(Comment comment) {
        this.commentId = comment.getCommentId();
        this.issueId = comment.getIssue().getIssueId();
        this.userId = comment.getUser().getUserId();
        this.username = comment.getUser().getUsername();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }

    public Long getCommentId() {
        return commentId;
    }

    public Long getIssueId() {
        return issueId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
