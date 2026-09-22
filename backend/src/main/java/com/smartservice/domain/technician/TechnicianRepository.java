package com.smartservice.domain.technician;

import com.smartservice.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {
    Optional<Technician> findByUser(User user);
    Optional<Technician> findByUserId(Long userId);
    Optional<Technician> findByEmail(String email);
}
