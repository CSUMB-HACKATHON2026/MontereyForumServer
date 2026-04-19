package com.MCF.backend.repository;

import com.MCF.backend.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * Repository for managing Issue entities.
 *
 * @author MCF Team
 * @version 0.1.0
 */
public interface IssueRepository extends JpaRepository<Issue, UUID> {

    /**
     * Finds all issues belonging to a specific user.
     *
     * @param userId the ID of the user
     * @return a list of issues belonging to the user
     */
    List<Issue> findByUser_Id(UUID userId);

    /**
     * Finds all issues with a specific category.
     *
     * @param category the category to filter by
     * @return a list of issues with the given category
     */
    List<Issue> findByCategory(String category);

    /**
     * Finds all issues with a specific status.
     *
     * @param status the status to filter by
     * @return a list of issues with the given status
     */
    List<Issue> findByStatus(String status);
}