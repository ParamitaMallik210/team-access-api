package com.teamaccess.team_access_api.repository;

import com.teamaccess.team_access_api.entity.Role;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {
}
