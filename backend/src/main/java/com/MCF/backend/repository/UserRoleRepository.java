package com.MCF.backend.repository;

import com.MCF.backend.model.UserRole;
import com.MCF.backend.model.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * Repository for managing UserRole entities.
 *
 * @author MCF Team
 * @version 0.1.0
 */
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    /**
     * Finds all roles assigned to a specific user.
     *
     * @param userId the ID of the user
     * @return a list of roles assigned to the user
     */
    List<UserRole> findByUser_Id(UUID userId);
}