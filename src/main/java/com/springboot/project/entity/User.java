package com.springboot.project.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(name = "account_number")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountNumber;

    @NotBlank(message = "Customer name is required")
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Telephone is required")
    @Pattern(regexp = "^(?:\\D*\\d){10}\\D*$", message = "Telephone must contain exactly 10 digits")
    private String telephone;

    @Enumerated(EnumType.STRING)
    private userType type =  userType.CUSTOMER;

    @Column(name = "created_at")
    private LocalDateTime createdAt =  LocalDateTime.now();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bill> bills;

    public User(String name, String username, String email, String password, String address, String telephone) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.address = address;
        this.telephone = telephone;
    }

    public enum userType {
        ADMIN, STAFF, CUSTOMER
    }
}
