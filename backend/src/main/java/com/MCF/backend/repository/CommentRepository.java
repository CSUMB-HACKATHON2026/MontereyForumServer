package com.MCF.backend.repository;

import com.MCF.backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Comment entities.
 *
 * @author MCF Team
 * @version 0.1.0
 */
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * Finds all comments for a specific issue.
     *
     * @param issueId the ID of the issue
     * @return a list of comments for the given issue
     */
    List<Comment> findByIssue_Id(UUID issueId);

    /**
     * Finds a comment by its ID and the user who created it.
     *
     * @param id the ID of the comment
     * @param userId the ID of the user
     * @return the comment matching the given ID and user
     */
    Optional<Comment> findByIdAndUser_Id(UUID id, UUID userId);
}