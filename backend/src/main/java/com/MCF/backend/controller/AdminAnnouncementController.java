package com.MCF.backend.controller;

import com.MCF.backend.dto.request.CreateAnnouncementRequest;
import com.MCF.backend.dto.response.IssueResponse;
import com.MCF.backend.service.IssueService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/announcements")
@CrossOrigin
public class AdminAnnouncementController {

    private final IssueService issueService;

    public AdminAnnouncementController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public IssueResponse createAnnouncement(Authentication authentication, @RequestBody CreateAnnouncementRequest body) {
        long userId = Long.parseLong(authentication.getPrincipal().toString());
        return issueService.createAnnouncementIssue(userId, body.getTitle(), body.getDescription());
    }
}
