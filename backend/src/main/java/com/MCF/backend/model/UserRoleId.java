package com.MCF.backend.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import java.util.Objects;

/**
 * Composite primary key for the user_roles table.
 *
 * @author MCF Team
 * @version 0.1.0
 */
@Embeddable
public class UserRoleId implements Serializable {

    private UUID userId;
    private String role;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRoleId)) return false;
        UserRoleId that = (UserRoleId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() { return Objects.hash(userId, role); }
}