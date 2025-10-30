package com.project.inklink.repository;

import com.project.inklink.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Basic CRUD operations
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Boolean existsByEmail(String email);

    Boolean existsByUsername(String username);

    // User profile stats
    @Query("SELECT COUNT(s) FROM Story s WHERE s.author.id = :userId AND s.status = 'PUBLISHED'")
    Long countPublishedStoriesByUser(@Param("userId") Long userId);

    // Find users by role with pagination
    Page<User> findByRole(String role, Pageable pageable);

    // Search users by username or email
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

    // Find active users
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    List<User> findActiveUsers();

    // Performance optimized query for user details with stories count
    @Query("SELECT u, COUNT(s) as storyCount FROM User u LEFT JOIN Story s ON s.author = u AND s.status = 'PUBLISHED' WHERE u.id = :userId GROUP BY u")
    Optional<Object[]> findUserWithStats(@Param("userId")Long userId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.stories WHERE u.id = :id")
    Optional<User> findByIdWithStories(@Param("id") Long id);
}