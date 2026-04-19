package com.MCF.backend.repository;

import com.MCF.backend.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    List<Issue> findByUser_UserId(Long userId);

    List<Issue> findByCategory_CategoryId(Long categoryId);

    List<Issue> findByCategory_Name(String name);

    List<Issue> findByStatus(String status);

    List<Issue> findByUser_UserIdAndStatus(Long userId, String status);
}