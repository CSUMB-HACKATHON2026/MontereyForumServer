package com.MCF.backend.service;

import com.MCF.backend.dto.request.CreateCommentRequest;
import com.MCF.backend.dto.request.UpdateCommentRequest;
import com.MCF.backend.dto.response.CommentResponse;
import com.MCF.backend.model.Comment;
import com.MCF.backend.model.Issue;
import com.MCF.backend.model.User;
import com.MCF.backend.repository.CommentRepository;
import com.MCF.backend.repository.IssueRepository;
import com.MCF.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    public CommentService(
            CommentRepository commentRepository,
            IssueRepository issueRepository,
            UserRepository userRepository
    ) {
        this.commentRepository = commentRepository;
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
    }

    public List<CommentResponse> getCommentsByIssue(Long issueId) {
        return commentRepository.findByIssue_IssueId(issueId)
                .stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }

    public CommentResponse createComment(Long issueId, CreateCommentRequest request) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment();
        comment.setIssue(issue);
        comment.setUser(user);
        comment.setContent(request.getContent());

        return new CommentResponse(commentRepository.save(comment));
    }

    public CommentResponse updateComment(Long commentId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setContent(request.getContent());

        return new CommentResponse(commentRepository.save(comment));
    }

    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        commentRepository.delete(comment);
    }
}
