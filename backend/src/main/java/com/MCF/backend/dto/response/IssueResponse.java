package com.MCF.backend.dto.response;

import com.MCF.backend.model.Issue;
import com.MCF.backend.model.IssueImage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class IssueResponse {
    private Long issueId;
    private Long userId;
    private String username;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private String status;
    private String locationText;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public IssueResponse() {
    }

    public IssueResponse(Issue issue, List<IssueImage> images) {
        this.issueId = issue.getIssueId();
        this.userId = issue.getUser().getUserId();
        this.username = issue.getUser().getUsername();
        this.categoryId = issue.getCategory().getCategoryId();
        this.categoryName = issue.getCategory().getName();
        this.title = issue.getTitle();
        this.description = issue.getDescription();
        this.status = issue.getStatus();
        this.locationText = issue.getLocationText();
        this.imageUrls = images.stream()
                .map(IssueImage::getImageUrl)
                .collect(Collectors.toList());
        this.createdAt = issue.getCreatedAt();
        this.updatedAt = issue.getUpdatedAt();
    }

    public Long getIssueId() {
        return issueId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getLocationText() {
        return locationText;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}