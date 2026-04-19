package com.MCF.backend.controller;

import com.MCF.backend.dto.request.CreateIssueRequest;
import com.MCF.backend.dto.request.UpdateIssueRequest;
import com.MCF.backend.dto.response.IssueResponse;
import com.MCF.backend.service.IssueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping
    public List<IssueResponse> getAllIssues() {
        return issueService.getAllIssues();
    }

    @GetMapping("/{issueId}")
    public IssueResponse getIssueById(@PathVariable Long issueId) {
        return issueService.getIssueById(issueId);
    }

    @GetMapping("/category/{categoryId}")
    public List<IssueResponse> getIssuesByCategory(@PathVariable Long categoryId) {
        return issueService.getIssuesByCategory(categoryId);
    }

    @GetMapping("/user/{userId}")
    public List<IssueResponse> getIssuesByUser(@PathVariable Long userId) {
        return issueService.getIssuesByUser(userId);
    }

    @GetMapping("/status/{status}")
    public List<IssueResponse> getIssuesByStatus(@PathVariable String status) {
        return issueService.getIssuesByStatus(status);
    }

    @PostMapping
    public IssueResponse createIssue(@RequestBody CreateIssueRequest request) {
        return issueService.createIssue(request);
    }

    @PutMapping("/{issueId}")
    public IssueResponse updateIssue(@PathVariable Long issueId,
                                     @RequestBody UpdateIssueRequest request) {
        return issueService.updateIssue(issueId, request);
    }

    @DeleteMapping("/{issueId}")
    public void deleteIssue(@PathVariable Long issueId) {
        issueService.deleteIssue(issueId);
    }
}
