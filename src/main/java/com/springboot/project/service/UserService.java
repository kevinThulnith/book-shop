package com.springboot.project.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import com.springboot.project.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.springboot.project.entity.User;
import lombok.RequiredArgsConstructor;
import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User registerUser(User user) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }
        
        // Encode password and save user
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setType(User.userType.CUSTOMER); // Default type
        return userRepository.save(user);
    }
    
    public User createUser(User user, User.userType userType) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }
        
        // Encode password and save user with specified type
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setType(userType);
        return userRepository.save(user);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public boolean validateUser(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return passwordEncoder.matches(password, user.get().getPassword());
        }
        return false;
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getUsersByType(User.userType type) {
        return userRepository.findByTypeOrderByCreatedAtDesc(type);
    }
    
    public List<User> getAllCustomers() {
        return userRepository.findByTypeOrderByCreatedAtDesc(User.userType.CUSTOMER);
    }
    
    public List<User> getAllStaff() {
        return userRepository.findByTypeOrderByCreatedAtDesc(User.userType.STAFF);
    }
    
    public List<User> getAllAdmins() {
        return userRepository.findByTypeOrderByCreatedAtDesc(User.userType.ADMIN);
    }
    
    public User findByAccountNumber(Long accountNumber) {
        return userRepository.findById(accountNumber)
            .orElseThrow(() -> new RuntimeException("User not found!"));
    }
    
    public User updateProfile(User currentUser, User updatedUser) {
        currentUser.setName(updatedUser.getName());
        currentUser.setEmail(updatedUser.getEmail());
        currentUser.setAddress(updatedUser.getAddress());
        currentUser.setTelephone(updatedUser.getTelephone());
        
        // Check if email is being changed and if it's already taken by another user
        if (!currentUser.getEmail().equals(updatedUser.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(updatedUser.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getAccountNumber().equals(currentUser.getAccountNumber())) {
                throw new RuntimeException("Email already exists!");
            }
        }
        
        return userRepository.save(currentUser);
    }
    
    public User updateUserAsAdmin(Long accountNumber, User updatedUser, User.userType newType) {
        User existingUser = findByAccountNumber(accountNumber);
        
        // Check if email is being changed and if it's already taken by another user
        if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
            Optional<User> emailCheck = userRepository.findByEmail(updatedUser.getEmail());
            if (emailCheck.isPresent() && !emailCheck.get().getAccountNumber().equals(accountNumber)) {
                throw new RuntimeException("Email already exists!");
            }
        }
        
        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setAddress(updatedUser.getAddress());
        existingUser.setTelephone(updatedUser.getTelephone());
        existingUser.setType(newType);
        
        return userRepository.save(existingUser);
    }
    
    public User updateUserAsStaff(Long accountNumber, User updatedUser) {
        User existingUser = findByAccountNumber(accountNumber);
        
        // Staff can only update customers
        if (existingUser.getType() != User.userType.CUSTOMER) {
            throw new RuntimeException("Staff can only update customer profiles!");
        }
        
        // Check if email is being changed and if it's already taken by another user
        if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
            Optional<User> emailCheck = userRepository.findByEmail(updatedUser.getEmail());
            if (emailCheck.isPresent() && !emailCheck.get().getAccountNumber().equals(accountNumber)) {
                throw new RuntimeException("Email already exists!");
            }
        }
        
        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setAddress(updatedUser.getAddress());
        existingUser.setTelephone(updatedUser.getTelephone());
        
        return userRepository.save(existingUser);
    }
}
