package com.MCF.backend.service;

import com.MCF.backend.model.Comment;
import com.MCF.backend.model.Issue;
import com.MCF.backend.model.Profile;
import com.MCF.backend.repository.CommentRepository;
import com.MCF.backend.repository.IssueRepository;
import com.MCF.backend.repository.ProfileRepository;
import com.MCF.backend.exception.ResourceNotFoundException;
import com.MCF.backend.exception.ForbiddenException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

/**
 * Service class for managing forum comments on city issues.
 * Handles business logic for creating, retrieving, updating, and deleting comments.
 *
 * @author MCF Team
 * @version 0.1.0
 */
@Service
public class ForumService {

    private final CommentRepository commentRepository;
    private final IssueRepository issueRepository;
    private final ProfileRepository profileRepository;

    /**
     * Constructs a ForumService with the given repositories.
     *
     * @param commentRepository the repository used to manage comments
     * @param issueRepository the repository used to manage issues
     * @param profileRepository the repository used to manage profiles
     */
    public ForumService(CommentRepository commentRepository, IssueRepository issueRepository, ProfileRepository profileRepository) {
        this.commentRepository = commentRepository;
        this.issueRepository = issueRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * Retrieves all comments for a specific issue.
     *
     * @param issueId the ID of the issue
     * @return a list of comments for the given issue
     */
    public List<Comment> getByIssue(UUID issueId) {
        return commentRepository.findByIssue_Id(issueId);
    }

    /**
     * Creates a new comment on an issue.
     *
     * @param issueId the ID of the issue to comment on
     * @param body the content of the comment
     * @param userId the ID of the authenticated user
     * @return the created comment
     * @throws ResourceNotFoundException if the issue or profile is not found
     */
    public Comment create(UUID issueId, String body, UUID userId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        Comment comment = new Comment();
        comment.setIssue(issue);
        comment.setUser(profile);
        comment.setBody(body);

        return commentRepository.save(comment);
    }

    /**
     * Updates the body of a comment owned by the authenticated user.
     *
     * @param commentId the ID of the comment to update
     * @param body the new content of the comment
     * @param userId the ID of the authenticated user
     * @return the updated comment
     * @throws ForbiddenException if the user does not own the comment
     */
    public Comment update(UUID commentId, String body, UUID userId) {
        Comment comment = commentRepository.findByIdAndUser_Id(commentId, userId)
                .orElseThrow(() -> new ForbiddenException("Not authorized to update this comment"));

        comment.setBody(body);
        return commentRepository.save(comment);
    }

    /**
     * Deletes a comment owned by the authenticated user.
     *
     * @param commentId the ID of the comment to delete
     * @param userId the ID of the authenticated user
     * @throws ForbiddenException if the user does not own the comment
     */
    public void delete(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findByIdAndUser_Id(commentId, userId)
                .orElseThrow(() -> new ForbiddenException("Not authorized to delete this comment"));

        commentRepository.deleteById(comment.getId());
    }
}