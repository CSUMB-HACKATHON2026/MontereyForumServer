package com.MCF.backend.controller;

import com.MCF.backend.model.Comment;
import com.MCF.backend.service.ForumService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for handling forum comment endpoints nested under issues.
 * Provides endpoints for creating, retrieving, updating, and deleting comments.
 *
 * @author MCF Team
 * @version 0.1.0
 */
@RestController
@RequestMapping("/issues/{issueId}/comments")
public class ForumController {

    private final ForumService forumService;

    /**
     * Constructs a ForumController with the given service.
     *
     * @param forumService the service used to manage forum comments
     */
    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    /**
     * Retrieves all comments for a specific issue.
     *
     * @param issueId the ID of the issue
     * @return a list of comments for the given issue
     */
    @GetMapping
    public ResponseEntity<List<Comment>> getByIssue(@PathVariable UUID issueId) {
        return ResponseEntity.ok(forumService.getByIssue(issueId));
    }

    /**
     * Creates a new comment on an issue.
     *
     * @param issueId the ID of the issue to comment on
     * @param body a map containing the comment body
     * @param userId the ID of the authenticated user from the JWT
     * @return the created comment with a 201 status
     */
    @PostMapping
    public ResponseEntity<Comment> create(@PathVariable UUID issueId,
                                          @RequestBody Map<String, String> body,
                                          @RequestAttribute("userId") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(forumService.create(issueId, body.get("body"), userId));
    }

    /**
     * Updates the body of a comment owned by the authenticated user.
     *
     * @param commentId the ID of the comment to update
     * @param body a map containing the new comment body
     * @param userId the ID of the authenticated user from the JWT
     * @return the updated comment
     */
    @PatchMapping("/{commentId}")
    public ResponseEntity<Comment> update(@PathVariable UUID commentId,
                                          @RequestBody Map<String, String> body,
                                          @RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(forumService.update(commentId, body.get("body"), userId));
    }

    /**
     * Deletes a comment owned by the authenticated user.
     *
     * @param commentId the ID of the comment to delete
     * @param userId the ID of the authenticated user from the JWT
     * @return a 204 no content response
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID commentId,
                                       @RequestAttribute("userId") UUID userId) {
        forumService.delete(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}