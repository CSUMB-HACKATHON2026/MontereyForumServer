package com.MCF.backend.repository;

import com.MCF.backend.model.Issue;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "category"})
    List<Issue> findAll();

    @Override
    @EntityGraph(attributePaths = {"user", "category"})
    Optional<Issue> findById(Long issueId);

    @EntityGraph(attributePaths = {"user", "category"})
    List<Issue> findByUser_UserId(Long userId);

    @EntityGraph(attributePaths = {"user", "category"})
    List<Issue> findByCategory_CategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"user", "category"})
    List<Issue> findByCategory_Name(String name);

    @EntityGraph(attributePaths = {"user", "category"})
    List<Issue> findByStatus(String status);

    @EntityGraph(attributePaths = {"user", "category"})
    List<Issue> findByUser_UserIdAndStatus(Long userId, String status);
}