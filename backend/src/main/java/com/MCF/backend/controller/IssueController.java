package com.MCF.backend.controller;

import com.MCF.backend.model.Issue;
import com.MCF.backend.service.IssueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for handling city issue endpoints.
 * Provides endpoints for creating, retrieving, updating, and deleting issues.
 *
 * @author MCF Team
 * @version 0.1.0
 */
@RestController
@RequestMapping("/issues")
public class IssueController {

    private final IssueService issueService;

    /**
     * Constructs an IssueController with the given service.
     *
     * @param issueService the service used to manage issues
     */
    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    /**
     * Retrieves all issues.
     *
     * @return a list of all issues
     */
    @GetMapping
    public ResponseEntity<List<Issue>> getAll() {
        return ResponseEntity.ok(issueService.getAll());
    }

    /**
     * Retrieves an issue by its ID.
     *
     * @param id the ID of the issue to retrieve
     * @return the issue with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Issue> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(issueService.getById(id));
    }

    /**
     * Creates a new issue for the authenticated user.
     *
     * @param issue the issue to create
     * @param userId the ID of the authenticated user from the JWT
     * @return the created issue with a 201 status
     */
    @PostMapping
    public ResponseEntity<Issue> create(@RequestBody Issue issue,
                                        @RequestAttribute("userId") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issueService.create(issue, userId));
    }

    /**
     * Partially updates an issue owned by the authenticated user.
     *
     * @param id the ID of the issue to update
     * @param updates the fields to update
     * @param userId the ID of the authenticated user from the JWT
     * @return the updated issue
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Issue> patch(@PathVariable UUID id,
                                       @RequestBody Issue updates,
                                       @RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(issueService.patch(id, updates, userId));
    }

    /**
     * Updates the status of an issue. Admin only.
     *
     * @param id the ID of the issue to update
     * @param body a map containing the new status
     * @return the updated issue
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Issue> updateStatus(@PathVariable UUID id,
                                              @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(issueService.updateStatus(id, body.get("status")));
    }

    /**
     * Deletes an issue owned by the authenticated user.
     *
     * @param id the ID of the issue to delete
     * @param userId the ID of the authenticated user from the JWT
     * @return a 204 no content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @RequestAttribute("userId") UUID userId) {
        issueService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}