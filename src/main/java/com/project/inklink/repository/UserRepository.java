package com.project.inklink.repository;

import com.project.inklink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by username
    Optional<User> findByUsername(String username);

    // Find user by email
    Optional<User> findByEmail(String email);

    // Check if username exists
    Boolean existsByUsername(String username);

    // Check if email exists
    Boolean existsByEmail(String email);

    // Find users by role
    List<User> findByRole(String role);

    // Search users by username (partial match)
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    List<User> findByUsernameContainingIgnoreCase(@Param("username") String username);

    // Find users with stories count
    @Query("SELECT u, COUNT(s) as storyCount FROM User u LEFT JOIN u.stories s GROUP BY u ORDER BY storyCount DESC")
    List<Object[]> findUsersWithStoryCount();

    // Find active users (users who have published stories)
    @Query("SELECT DISTINCT u FROM User u JOIN u.stories s WHERE s.status = 'PUBLISHED'")
    List<User> findActiveUsers();
}