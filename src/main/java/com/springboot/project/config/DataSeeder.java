package com.springboot.project.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.springboot.project.entity.User;
import com.springboot.project.repository.UserRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner init(@Autowired UserRepository userRepository, 
                          @Autowired PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if users already exist
            if (userRepository.count() == 0) {
                // Create an admin user
                User admin = new User();
                admin.setName("System Administrator");
                admin.setUsername("admin");
                admin.setEmail("admin@bookshop.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setAddress("123 Admin Street, Admin City");
                admin.setTelephone("1234567890");
                admin.setType(User.userType.ADMIN);
                userRepository.save(admin);

                // Create a staff user
                User staff = new User();
                staff.setName("John Staff");
                staff.setUsername("staff");
                staff.setEmail("staff@bookshop.com");
                staff.setPassword(passwordEncoder.encode("staff123"));
                staff.setAddress("456 Staff Avenue, Staff City");
                staff.setTelephone("2345678901");
                staff.setType(User.userType.STAFF);
                userRepository.save(staff);

                // Create some sample customers
                User customer1 = new User();
                customer1.setName("Alice Customer");
                customer1.setUsername("alice");
                customer1.setEmail("alice@example.com");
                customer1.setPassword(passwordEncoder.encode("customer123"));
                customer1.setAddress("789 Customer Road, Customer Town");
                customer1.setTelephone("3456789012");
                customer1.setType(User.userType.CUSTOMER);
                userRepository.save(customer1);

                User customer2 = new User();
                customer2.setName("Bob Customer");
                customer2.setUsername("bob");
                customer2.setEmail("bob@example.com");
                customer2.setPassword(passwordEncoder.encode("customer123"));
                customer2.setAddress("321 Customer Lane, Customer Village");
                customer2.setTelephone("4567890123");
                customer2.setType(User.userType.CUSTOMER);
                userRepository.save(customer2);

                User customer3 = new User();
                customer3.setName("Charlie Customer");
                customer3.setUsername("charlie");
                customer3.setEmail("charlie@example.com");
                customer3.setPassword(passwordEncoder.encode("customer123"));
                customer3.setAddress("654 Customer Drive, Customer City");
                customer3.setTelephone("5678901234");
                customer3.setType(User.userType.CUSTOMER);
                userRepository.save(customer3);

                System.out.println("Sample data seeded successfully!");
                System.out.println("Admin login: admin / admin123");
                System.out.println("Staff login: staff / staff123");
                System.out.println("Customer login: alice / customer123 or bob / customer123");
            }
        };
    }
}
