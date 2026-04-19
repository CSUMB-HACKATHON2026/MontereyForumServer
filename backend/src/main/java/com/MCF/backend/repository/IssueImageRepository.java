package com.MCF.backend.repository;

import com.MCF.backend.model.IssueImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueImageRepository extends JpaRepository<IssueImage, Long> {

    List<IssueImage> findByIssue_IssueId(Long issueId);

    List<IssueImage> findByIssue_IssueIdIn(List<Long> issueIds);

    void deleteByIssue_IssueId(Long issueId);
}
