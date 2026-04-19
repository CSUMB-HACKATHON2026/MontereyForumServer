package com.MCF.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByIssue_IssueId(Long issueId);

    List<Comment> findByUser_UserId(Long userId);

    Optional<Comment> findByCommentIdAndUser_UserId(Long commentId, Long userId);
}
