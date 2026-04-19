package com.MCF.backend.service;

import com.MCF.backend.dto.request.CreateIssueRequest;
import com.MCF.backend.dto.request.UpdateIssueRequest;
import com.MCF.backend.dto.response.IssueResponse;
import com.MCF.backend.model.Category;
import com.MCF.backend.model.Issue;
import com.MCF.backend.model.IssueImage;
import com.MCF.backend.model.User;
import com.MCF.backend.repository.CategoryRepository;
import com.MCF.backend.repository.IssueImageRepository;
import com.MCF.backend.repository.IssueRepository;
import com.MCF.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final IssueImageRepository issueImageRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Value("${app.announcements.category-id:1}")
    private long announcementsCategoryId;

    public IssueService(
            IssueRepository issueRepository,
            IssueImageRepository issueImageRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository
    ) {
        this.issueRepository = issueRepository;
        this.issueImageRepository = issueImageRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<IssueResponse> getAllIssues() {
        return toResponses(issueRepository.findAll());
    }

    public List<IssueResponse> getBulletinIssues() {
        List<Issue> bulletins = issueRepository.findAll()
                .stream()
                .filter(issue -> !Objects.equals(issue.getCategory().getCategoryId(), announcementsCategoryId))
                .collect(Collectors.toList());
        return toResponses(bulletins);
    }

    public List<IssueResponse> getAnnouncementIssues() {
        return toResponses(issueRepository.findByCategory_CategoryId(announcementsCategoryId));
    }

    public IssueResponse getIssueById(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));
        return toResponse(issue);
    }

    public List<IssueResponse> getIssuesByCategory(Long categoryId) {
        return toResponses(issueRepository.findByCategory_CategoryId(categoryId));
    }

    public List<IssueResponse> getIssuesByUser(Long userId) {
        return toResponses(issueRepository.findByUser_UserId(userId));
    }

    public List<IssueResponse> getIssuesByStatus(String status) {
        return toResponses(issueRepository.findByStatus(status));
    }

    @Transactional
    public IssueResponse createIssueForUser(long userId, CreateIssueRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Issue issue = new Issue();
        issue.setUser(user);
        issue.setCategory(category);
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "OPEN" : request.getStatus());
        issue.setLocationText(request.getLocationText());

        Issue savedIssue = issueRepository.save(issue);

        if (request.getImageUrls() != null) {
            for (String imageUrl : request.getImageUrls()) {
                if (imageUrl == null || imageUrl.isBlank()) {
                    continue;
                }
                String trimmed = imageUrl.trim();
                if (trimmed.length() > 255) {
                    trimmed = trimmed.substring(0, 255);
                }
                IssueImage image = new IssueImage();
                image.setIssue(savedIssue);
                image.setImageUrl(trimmed);
                issueImageRepository.save(image);
            }
        }

        return toResponse(savedIssue);
    }

    @Transactional
    public IssueResponse createAnnouncementIssue(long authorUserId, String title, String description) {
        if (title == null || title.isBlank()) {
            throw new RuntimeException("Title is required");
        }
        if (description == null || description.isBlank()) {
            throw new RuntimeException("Description is required");
        }
        User user = userRepository.findById(authorUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Category category = categoryRepository.findById(announcementsCategoryId)
                .orElseThrow(() -> new RuntimeException(
                        "Announcement category not found. Set app.announcements.category-id in application.properties."));

        Issue issue = new Issue();
        issue.setUser(user);
        issue.setCategory(category);
        issue.setTitle(title.trim());
        issue.setDescription(description.trim());
        issue.setStatus("OPEN");
        issue.setLocationText(null);

        Issue savedIssue = issueRepository.save(issue);
        return toResponse(savedIssue);
    }

    @Transactional
    public IssueResponse updateIssue(Long issueId, UpdateIssueRequest request) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            issue.setCategory(category);
        }

        if (request.getTitle() != null) {
            issue.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            issue.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            issue.setStatus(request.getStatus());
        }

        if (request.getLocationText() != null) {
            issue.setLocationText(request.getLocationText());
        }

        Issue savedIssue = issueRepository.save(issue);

        if (request.getImageUrls() != null) {
            issueImageRepository.deleteByIssue_IssueId(issueId);

            for (String imageUrl : request.getImageUrls()) {
                IssueImage image = new IssueImage();
                image.setIssue(savedIssue);
                image.setImageUrl(imageUrl);
                issueImageRepository.save(image);
            }
        }

        return toResponse(savedIssue);
    }

    public void deleteIssue(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));
        issueRepository.delete(issue);
    }

    private IssueResponse toResponse(Issue issue) {
        List<IssueImage> images = issueImageRepository.findByIssue_IssueId(issue.getIssueId());
        return new IssueResponse(issue, images);
    }

    private List<IssueResponse> toResponses(List<Issue> issues) {
        if (issues.isEmpty()) {
            return List.of();
        }

        List<Long> issueIds = issues.stream()
                .map(Issue::getIssueId)
                .collect(Collectors.toList());

        Map<Long, List<IssueImage>> imagesByIssueId = issueImageRepository.findByIssue_IssueIdIn(issueIds)
                .stream()
                .collect(Collectors.groupingBy(image -> image.getIssue().getIssueId()));

        return issues.stream()
                .map(issue -> new IssueResponse(issue, imagesByIssueId.getOrDefault(issue.getIssueId(), List.of())))
                .collect(Collectors.toList());
    }
}