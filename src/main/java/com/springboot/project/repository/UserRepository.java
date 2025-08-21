package com.springboot.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.springboot.project.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Basic finder methods
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByAccountNumber(Long accountNumber);
    
    // Existence checks
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByAccountNumber(Long accountNumber);
    
    // Find by user type
    List<User> findByType(User.userType type);
    
    // Search users by name (case insensitive)
    List<User> findByNameContainingIgnoreCase(String name);
    
    // Find users by telephone
    Optional<User> findByTelephone(String telephone);
    boolean existsByTelephone(String telephone);
    
    // Find users by address (case insensitive)
    List<User> findByAddressContainingIgnoreCase(String address);
    
    // Find users created within date range
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find users by type and creation date
    List<User> findByTypeAndCreatedAtBetween(User.userType type, LocalDateTime startDate, LocalDateTime endDate);
    
    // Custom queries using @Query annotation
    
    // Find users with bills count
    @Query("SELECT u, SIZE(u.bills) as billCount FROM User u WHERE u.type = :userType")
    List<Object[]> getUsersWithBillCount(@Param("userType") User.userType userType);
    
    // Find customers with no bills
    @Query("SELECT u FROM User u WHERE u.type = :userType AND SIZE(u.bills) = 0")
    List<User> getCustomersWithNoBills(@Param("userType") User.userType userType);
    
    // Find active customers (customers with bills)
    @Query("SELECT DISTINCT u FROM User u JOIN u.bills b WHERE u.type = :userType")
    List<User> getActiveCustomers(@Param("userType") User.userType userType);
    
    // Count users by type
    @Query("SELECT COUNT(u) FROM User u WHERE u.type = :userType")
    Long countUsersByType(@Param("userType") User.userType userType);
    
    // Find users registered in last N days
    @Query("SELECT u FROM User u WHERE u.createdAt >= :dateThreshold ORDER BY u.createdAt DESC")
    List<User> getRecentUsers(@Param("dateThreshold") LocalDateTime dateThreshold);
    
    // Find users by partial email domain
    @Query("SELECT u FROM User u WHERE u.email LIKE %:domain%")
    List<User> getUsersByEmailDomain(@Param("domain") String domain);
    
    // Get user statistics by type
    @Query("SELECT u.type, COUNT(u) as userCount, " +
           "MIN(u.createdAt) as earliestRegistration, " +
           "MAX(u.createdAt) as latestRegistration " +
           "FROM User u GROUP BY u.type")
    List<Object[]> getUserStatisticsByType();
    
    // Find users who haven't logged in recently (assuming login tracking)
    @Query("SELECT u FROM User u WHERE u.type = :userType ORDER BY u.createdAt ASC")
    List<User> getOldestUsersByType(@Param("userType") User.userType userType);
    
    // Search users by multiple criteria
    @Query("SELECT u FROM User u WHERE " +
           "(:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:userType IS NULL OR u.type = :userType) AND " +
           "(:address IS NULL OR LOWER(u.address) LIKE LOWER(CONCAT('%', :address, '%')))")
    List<User> searchUsers(@Param("name") String name,
                          @Param("email") String email,
                          @Param("userType") User.userType userType,
                          @Param("address") String address);
    
    // Update user type
    @Modifying
    @Query("UPDATE User u SET u.type = :newType WHERE u.accountNumber = :accountNumber")
    int updateUserType(@Param("accountNumber") Long accountNumber, @Param("newType") User.userType newType);
    
    // Update user contact information
    @Modifying
    @Query("UPDATE User u SET u.telephone = :telephone, u.address = :address WHERE u.accountNumber = :accountNumber")
    int updateUserContact(@Param("accountNumber") Long accountNumber, 
                         @Param("telephone") String telephone, 
                         @Param("address") String address);
    
    // Find users by name and type
    List<User> findByNameContainingIgnoreCaseAndType(String name, User.userType type);
    
    // Find all admin and staff users
    List<User> findByTypeIn(List<User.userType> types);
    
    // Check if user exists by username or email
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username OR u.email = :email")
    boolean existsByUsernameOrEmail(@Param("username") String username, @Param("email") String email);
    
    // Get user count by registration month
    @Query("SELECT YEAR(u.createdAt) as year, MONTH(u.createdAt) as month, COUNT(u) as userCount " +
           "FROM User u WHERE u.type = :userType " +
           "GROUP BY YEAR(u.createdAt), MONTH(u.createdAt) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> getUserRegistrationReport(@Param("userType") User.userType userType);
}
