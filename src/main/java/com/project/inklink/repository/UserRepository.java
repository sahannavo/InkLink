package com.project.inklink.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<com.project.inklink.entity.User, Long> {
    Optional<com.project.inklink.entity.User> findByEmail(String email);

    Optional<com.project.inklink.entity.User> findByUsername(String username);

    Boolean existsByEmail(String email);

    Boolean existsByUsername(String username);

    // For user profile stats
    @Query("SELECT COUNT(s) FROM Story s WHERE s.author.id = :userId AND s.status = 'PUBLISHED'")
    Long countPublishedStoriesByUser(@Param("userId") Long userId);
}