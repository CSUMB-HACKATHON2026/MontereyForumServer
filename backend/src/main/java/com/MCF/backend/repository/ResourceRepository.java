package com.MCF.backend.repository;

import com.MCF.backend.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * Repository for managing Resource entities.
 *
 * @author MCF Team
 * @version 0.1.0
 */
public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    /**
     * Finds all published resources.
     *
     * @return a list of all published resources
     */
    List<Resource> findByPublishedTrue();

    /**
     * Finds all resources with a specific status.
     *
     * @param status the status to filter by
     * @return a list of resources with the given status
     */
    List<Resource> findByStatus(String status);
}