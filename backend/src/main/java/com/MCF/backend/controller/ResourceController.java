package com.MCF.backend.controller;

import com.MCF.backend.model.Resource;
import com.MCF.backend.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 * Controller for handling resource and guide endpoints.
 * Provides endpoints for browsing, recommending, and reviewing resources.
 *
 * @author MCF Team
 * @version 0.1.0
 */
@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;

    /**
     * Constructs a ResourceController with the given service.
     *
     * @param resourceService the service used to manage resources
     */
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    /**
     * Retrieves all published resources.
     *
     * @return a list of all published resources
     */
    @GetMapping
    public ResponseEntity<List<Resource>> getAll() {
        return ResponseEntity.ok(resourceService.getAllPublished());
    }

    /**
     * Retrieves a resource by its ID.
     *
     * @param id the ID of the resource to retrieve
     * @return the resource with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(resourceService.getById(id));
    }

    /**
     * Retrieves all pending resource recommendations. Admin only.
     *
     * @return a list of all pending resources
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Resource>> getPending() {
        return ResponseEntity.ok(resourceService.getPending());
    }

    /**
     * Submits a resource recommendation from the authenticated user.
     *
     * @param resource the resource to recommend
     * @param userId the ID of the authenticated user from the JWT
     * @return the created resource recommendation with a 201 status
     */
    @PostMapping("/recommend")
    public ResponseEntity<Resource> recommend(@RequestBody Resource resource,
                                              @RequestAttribute("userId") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.recommend(resource, userId));
    }

    /**
     * Approves a resource recommendation. Admin only.
     *
     * @param id the ID of the resource to approve
     * @return the approved resource
     */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(resourceService.approve(id));
    }

    /**
     * Denies a resource recommendation. Admin only.
     *
     * @param id the ID of the resource to deny
     * @return the denied resource
     */
    @PatchMapping("/{id}/deny")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> deny(@PathVariable UUID id) {
        return ResponseEntity.ok(resourceService.deny(id));
    }

    /**
     * Deletes a resource by its ID. Admin only.
     *
     * @param id the ID of the resource to delete
     * @return a 204 no content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        resourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}