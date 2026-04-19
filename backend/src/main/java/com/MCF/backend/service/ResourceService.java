package com.MCF.backend.service;

import com.MCF.backend.model.Profile;
import com.MCF.backend.model.Resource;
import com.MCF.backend.repository.ProfileRepository;
import com.MCF.backend.repository.ResourceRepository;
import com.MCF.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

/**
 * Service class for managing resources and guides.
 * Handles business logic for creating, retrieving, and reviewing resources.
 *
 * @author MCF Team
 * @version 0.1.0
 */
@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ProfileRepository profileRepository;

    /**
     * Constructs a ResourceService with the given repositories.
     *
     * @param resourceRepository the repository used to manage resources
     * @param profileRepository the repository used to manage profiles
     */
    public ResourceService(ResourceRepository resourceRepository, ProfileRepository profileRepository) {
        this.resourceRepository = resourceRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * Retrieves all published resources.
     *
     * @return a list of all published resources
     */
    public List<Resource> getAllPublished() {
        return resourceRepository.findByPublishedTrue();
    }

    /**
     * Retrieves all pending resources. Admin only.
     *
     * @return a list of all pending resources
     */
    public List<Resource> getPending() {
        return resourceRepository.findByStatus("pending");
    }

    /**
     * Retrieves a resource by its ID.
     *
     * @param id the ID of the resource to retrieve
     * @return the resource with the given ID
     * @throws ResourceNotFoundException if no resource is found with the given ID
     */
    public Resource getById(UUID id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
    }

    /**
     * Submits a resource recommendation from a user.
     * Sets status to pending and published to false until approved by an admin.
     *
     * @param resource the resource to recommend
     * @param userId the ID of the authenticated user
     * @return the created resource recommendation
     * @throws ResourceNotFoundException if no profile is found for the user
     */
    public Resource recommend(Resource resource, UUID userId) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        resource.setCreatedBy(profile);
        resource.setStatus("pending");
        resource.setPublished(false);
        return resourceRepository.save(resource);
    }

    /**
     * Approves a resource recommendation. Admin only.
     *
     * @param id the ID of the resource to approve
     * @return the approved resource
     * @throws ResourceNotFoundException if no resource is found with the given ID
     */
    public Resource approve(UUID id) {
        Resource resource = getById(id);
        resource.setStatus("approved");
        resource.setPublished(true);
        return resourceRepository.save(resource);
    }

    /**
     * Denies a resource recommendation. Admin only.
     *
     * @param id the ID of the resource to deny
     * @return the denied resource
     * @throws ResourceNotFoundException if no resource is found with the given ID
     */
    public Resource deny(UUID id) {
        Resource resource = getById(id);
        resource.setStatus("denied");
        resource.setPublished(false);
        return resourceRepository.save(resource);
    }

    /**
     * Deletes a resource by its ID. Admin only.
     *
     * @param id the ID of the resource to delete
     * @throws ResourceNotFoundException if no resource is found with the given ID
     */
    public void delete(UUID id) {
        getById(id);
        resourceRepository.deleteById(id);
    }
}