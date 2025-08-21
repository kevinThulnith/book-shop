package com.springboot.project.service;

import com.springboot.project.entity.User;
import com.springboot.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Basic CRUD Operations
    
    /**
     * Create a new user with encrypted password
     */
    public User createUser(User user) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        
        if (userRepository.existsByTelephone(user.getTelephone())) {
            throw new RuntimeException("Telephone number already exists: " + user.getTelephone());
        }
        
        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Get user by ID
     */
    public Optional<User> getUserById(Long accountNumber) {
        return userRepository.findByAccountNumber(accountNumber);
    }
    
    /**
     * Get user by username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Get user by email
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Get all users with pagination
     */
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    /**
     * Get all users as list
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Update user information
     */
    public User updateUser(Long accountNumber, User updatedUser) {
        User existingUser = userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("User not found with account number: " + accountNumber));
        
        // Check if new username/email conflicts with other users
        if (!existingUser.getUsername().equals(updatedUser.getUsername()) && 
            userRepository.existsByUsername(updatedUser.getUsername())) {
            throw new RuntimeException("Username already exists: " + updatedUser.getUsername());
        }
        
        if (!existingUser.getEmail().equals(updatedUser.getEmail()) && 
            userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new RuntimeException("Email already exists: " + updatedUser.getEmail());
        }
        
        if (!existingUser.getTelephone().equals(updatedUser.getTelephone()) && 
            userRepository.existsByTelephone(updatedUser.getTelephone())) {
            throw new RuntimeException("Telephone number already exists: " + updatedUser.getTelephone());
        }
        
        // Update fields
        existingUser.setName(updatedUser.getName());
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setAddress(updatedUser.getAddress());
        existingUser.setTelephone(updatedUser.getTelephone());
        existingUser.setType(updatedUser.getType());
        
        return userRepository.save(existingUser);
    }
    
    /**
     * Update user password
     */
    public void updatePassword(Long accountNumber, String newPassword) {
        User user = userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("User not found with account number: " + accountNumber));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    /**
     * Update user contact information
     */
    public void updateUserContact(Long accountNumber, String telephone, String address) {
        int updated = userRepository.updateUserContact(accountNumber, telephone, address);
        if (updated == 0) {
            throw new RuntimeException("User not found with account number: " + accountNumber);
        }
    }
    
    /**
     * Delete user by ID
     */
    public void deleteUser(Long accountNumber) {
        if (!userRepository.existsByAccountNumber(accountNumber)) {
            throw new RuntimeException("User not found with account number: " + accountNumber);
        }
        userRepository.deleteById(accountNumber);
    }
    
    // User Type Management
    
    /**
     * Get users by type
     */
    public List<User> getUsersByType(User.userType type) {
        return userRepository.findByType(type);
    }
    
    /**
     * Update user type
     */
    public void updateUserType(Long accountNumber, User.userType newType) {
        int updated = userRepository.updateUserType(accountNumber, newType);
        if (updated == 0) {
            throw new RuntimeException("User not found with account number: " + accountNumber);
        }
    }
    
    /**
     * Get all customers
     */
    public List<User> getAllCustomers() {
        return userRepository.findByType(User.userType.CUSTOMER);
    }
    
    /**
     * Get all staff members
     */
    public List<User> getAllStaff() {
        return userRepository.findByType(User.userType.STAFF);
    }
    
    /**
     * Get all admin users
     */
    public List<User> getAllAdmins() {
        return userRepository.findByType(User.userType.ADMIN);
    }
    
    // Search and Filter Operations
    
    /**
     * Search users by name
     */
    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Search users by address
     */
    public List<User> searchUsersByAddress(String address) {
        return userRepository.findByAddressContainingIgnoreCase(address);
    }
    
    /**
     * Search users by multiple criteria
     */
    public List<User> searchUsers(String name, String email, User.userType userType, String address) {
        return userRepository.searchUsers(name, email, userType, address);
    }
    
    /**
     * Get users by name and type
     */
    public List<User> getUsersByNameAndType(String name, User.userType type) {
        return userRepository.findByNameContainingIgnoreCaseAndType(name, type);
    }
    
    /**
     * Get users by email domain
     */
    public List<User> getUsersByEmailDomain(String domain) {
        return userRepository.getUsersByEmailDomain(domain);
    }
    
    // Business Logic Operations
    
    /**
     * Authenticate user
     */
    public boolean authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }
    
    /**
     * Check if user exists by username or email
     */
    public boolean userExists(String username, String email) {
        return userRepository.existsByUsernameOrEmail(username, email);
    }
    
    /**
     * Get active customers (customers with bills)
     */
    public List<User> getActiveCustomers() {
        return userRepository.getActiveCustomers(User.userType.CUSTOMER);
    }
    
    /**
     * Get customers with no bills
     */
    public List<User> getCustomersWithNoBills() {
        return userRepository.getCustomersWithNoBills(User.userType.CUSTOMER);
    }
    
    /**
     * Get users with bill count
     */
    public List<Object[]> getUsersWithBillCount(User.userType userType) {
        return userRepository.getUsersWithBillCount(userType);
    }
    
    // Date-based Operations
    
    /**
     * Get users created within date range
     */
    public List<User> getUsersCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.findByCreatedAtBetween(startDate, endDate);
    }
    
    /**
     * Get users by type and creation date
     */
    public List<User> getUsersByTypeAndDateRange(User.userType type, LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.findByTypeAndCreatedAtBetween(type, startDate, endDate);
    }
    
    /**
     * Get recently registered users (last N days)
     */
    public List<User> getRecentUsers(int days) {
        LocalDateTime dateThreshold = LocalDateTime.now().minusDays(days);
        return userRepository.getRecentUsers(dateThreshold);
    }
    
    /**
     * Get oldest users by type
     */
    public List<User> getOldestUsersByType(User.userType userType) {
        return userRepository.getOldestUsersByType(userType);
    }
    
    // Statistics and Analytics
    
    /**
     * Count users by type
     */
    public Long countUsersByType(User.userType userType) {
        return userRepository.countUsersByType(userType);
    }
    
    /**
     * Get user statistics by type
     */
    public List<Object[]> getUserStatisticsByType() {
        return userRepository.getUserStatisticsByType();
    }
    
    /**
     * Get user registration report by type
     */
    public List<Object[]> getUserRegistrationReport(User.userType userType) {
        return userRepository.getUserRegistrationReport(userType);
    }
    
    /**
     * Get total user count
     */
    public long getTotalUserCount() {
        return userRepository.count();
    }
    
    /**
     * Get user count by each type
     */
    public UserTypeCount getUserTypeCounts() {
        long adminCount = countUsersByType(User.userType.ADMIN);
        long staffCount = countUsersByType(User.userType.STAFF);
        long customerCount = countUsersByType(User.userType.CUSTOMER);
        
        return new UserTypeCount(adminCount, staffCount, customerCount);
    }
    
    // Validation Methods
    
    /**
     * Validate user data before creation/update
     */
    public void validateUser(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new RuntimeException("User name is required");
        }
        
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        
        if (user.getTelephone() == null || user.getTelephone().trim().isEmpty()) {
            throw new RuntimeException("Telephone is required");
        }
        
        if (user.getAddress() == null || user.getAddress().trim().isEmpty()) {
            throw new RuntimeException("Address is required");
        }
        
        // Validate email format
        if (!isValidEmail(user.getEmail())) {
            throw new RuntimeException("Invalid email format");
        }
        
        // Validate telephone format (should contain exactly 10 digits)
        if (!user.getTelephone().replaceAll("\\D", "").matches("\\d{10}")) {
            throw new RuntimeException("Telephone must contain exactly 10 digits");
        }
    }
    
    /**
     * Simple email validation
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    // Inner class for user type counts
    public static class UserTypeCount {
        private final long adminCount;
        private final long staffCount;
        private final long customerCount;
        
        public UserTypeCount(long adminCount, long staffCount, long customerCount) {
            this.adminCount = adminCount;
            this.staffCount = staffCount;
            this.customerCount = customerCount;
        }
        
        // Getters
        public long getAdminCount() { return adminCount; }
        public long getStaffCount() { return staffCount; }
        public long getCustomerCount() { return customerCount; }
        public long getTotalCount() { return adminCount + staffCount + customerCount; }
    }
}
