package com.MCF.backend.model;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Represents a role assigned to a user.
 * Role must be one of: user, admin.
 *
 * @author MCF Team
 * @version 0.1.0
 */
@Entity
@Table(name = "user_roles")
public class UserRole {

    @EmbeddedId
    private UserRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private Profile user;

    public UserRoleId getId() { return id; }
    public void setId(UserRoleId id) { this.id = id; }

    public Profile getUser() { return user; }
    public void setUser(Profile user) { this.user = user; }

    public String getRole() { return id.getRole(); }
}