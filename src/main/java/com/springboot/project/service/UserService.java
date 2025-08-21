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
}
