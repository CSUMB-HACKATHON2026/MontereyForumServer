package com.MCF.backend.service;

import com.MCF.backend.model.Issue;
import com.MCF.backend.model.Profile;
import com.MCF.backend.repository.IssueRepository;
import com.MCF.backend.repository.ProfileRepository;
import com.MCF.backend.exception.ResourceNotFoundException;
import com.MCF.backend.exception.ForbiddenException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

/**
 * Service class for managing city issues.
 * Handles business logic for creating, retrieving, updating, and deleting issues.
 *
 * @author MCF Team
 * @version 0.1.0
 */
@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final ProfileRepository profileRepository;

    /**
     * Constructs an IssueService with the given repositories.
     *
     * @param issueRepository the repository used to manage issues
     * @param profileRepository the repository used to manage profiles
     */
    public IssueService(IssueRepository issueRepository, ProfileRepository profileRepository) {
        this.issueRepository = issueRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * Retrieves all issues.
     *
     * @return a list of all issues
     */
    public List<Issue> getAll() {
        return issueRepository.findAll();
    }

    /**
     * Retrieves an issue by its ID.
     *
     * @param id the ID of the issue to retrieve
     * @return the issue with the given ID
     * @throws ResourceNotFoundException if no issue is found with the given ID
     */
    public Issue getById(UUID id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + id));
    }

    /**
     * Retrieves all issues belonging to a specific user.
     *
     * @param userId the ID of the user
     * @return a list of issues belonging to the user
     */
    public List<Issue> getByUser(UUID userId) {
        return issueRepository.findByUser_Id(userId);
    }

    /**
     * Creates a new issue for the authenticated user.
     *
     * @param issue the issue to create
     * @param userId the ID of the authenticated user
     * @return the created issue
     * @throws ResourceNotFoundException if no profile is found for the user
     */
    public Issue create(Issue issue, UUID userId) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        issue.setUser(profile);
        issue.setStatus("open");
        return issueRepository.save(issue);
    }

    /**
     * Updates the status of an issue. Admin only.
     *
     * @param id the ID of the issue to update
     * @param status the new status
     * @return the updated issue
     * @throws ResourceNotFoundException if no issue is found with the given ID
     */
    public Issue updateStatus(UUID id, String status) {
        Issue issue = getById(id);
        issue.setStatus(status);
        return issueRepository.save(issue);
    }

    /**
     * Partially updates an issue owned by the authenticated user.
     *
     * @param id the ID of the issue to update
     * @param updates the issue containing the fields to update
     * @param userId the ID of the authenticated user
     * @return the updated issue
     * @throws ForbiddenException if the user does not own the issue
     */
    public Issue patch(UUID id, Issue updates, UUID userId) {
        Issue existing = getById(id);

        if (!existing.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Not authorized to update this issue");
        }

        if (updates.getTitle() != null) existing.setTitle(updates.getTitle());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getCategory() != null) existing.setCategory(updates.getCategory());
        if (updates.getNeighborhood() != null) existing.setNeighborhood(updates.getNeighborhood());
        if (updates.getImageUrl() != null) existing.setImageUrl(updates.getImageUrl());

        return issueRepository.save(existing);
    }

    /**
     * Deletes an issue owned by the authenticated user.
     *
     * @param id the ID of the issue to delete
     * @param userId the ID of the authenticated user
     * @throws ForbiddenException if the user does not own the issue
     */
    public void delete(UUID id, UUID userId) {
        Issue existing = getById(id);

        if (!existing.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Not authorized to delete this issue");
        }

        issueRepository.deleteById(id);
    }
}