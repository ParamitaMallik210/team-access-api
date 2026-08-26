package com.teamaccess.team_access_api.repository;

import com.teamaccess.team_access_api.entity.Organization;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
}
