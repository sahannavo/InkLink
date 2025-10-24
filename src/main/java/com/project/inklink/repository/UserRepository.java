package com.project.inklink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<com.project.inklink.entity.User, Long>
{
    Optional<com.project.inklink.entity.User> findByEmail(String email);
    Optional<com.project.inklink.entity.User> findByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
}
