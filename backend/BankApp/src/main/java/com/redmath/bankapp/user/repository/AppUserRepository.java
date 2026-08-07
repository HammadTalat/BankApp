package com.redmath.bankapp.user.repository;

import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

  Optional<AppUser> findByEmail(String email);

  boolean existsByEmail(String email);

  List<AppUser> findAllByRoleAndApprovalStatusOrderByIdAsc(
      Role role,
      ApprovalStatus approvalStatus
  );

  boolean existsByEmailIgnoreCaseAndIdNot(
      String email,
      Long userId
  );

  long countByRoleAndApprovalStatus(
      Role role,
      ApprovalStatus approvalStatus
  );

}