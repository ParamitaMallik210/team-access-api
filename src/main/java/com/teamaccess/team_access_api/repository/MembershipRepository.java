package com.teamaccess.team_access_api.repository;

import com.teamaccess.team_access_api.entity.Membership;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {
}
