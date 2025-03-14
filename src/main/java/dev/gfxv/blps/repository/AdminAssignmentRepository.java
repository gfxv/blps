package dev.gfxv.blps.repository;

import dev.gfxv.blps.entity.AdminAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminAssignmentRepository extends JpaRepository<AdminAssignment, Long> {
    List<AdminAssignment> findByAdminId(Long adminId);
}
