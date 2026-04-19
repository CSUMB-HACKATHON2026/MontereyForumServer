package com.MCF.backend.repository;

import com.MCF.backend.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Profile entities.
 *
 * @author MCF Team
 * @version 0.1.0
 */
public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    /**
     * Finds a profile by its username.
     *
     * @param username the username to search for
     * @return the profile with the given username
     */
    Optional<Profile> findByUsername(String username);
}