package com.springboot.project.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.springboot.project.entity.User;
import com.springboot.project.entity.Item;
import com.springboot.project.repository.UserRepository;
import com.springboot.project.repository.ItemRepository;
import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner init(@Autowired UserRepository userRepository, 
                          @Autowired ItemRepository itemRepository,
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

                System.out.println("Sample users seeded successfully!");
                System.out.println("Admin login: admin / admin123");
                System.out.println("Staff login: staff / staff123");
                System.out.println("Customer login: alice / customer123 or bob / customer123");
            }

            // Check if items already exist
            if (itemRepository.count() == 0) {
                // Create sample items
                Item book1 = new Item();
                book1.setName("The Great Gatsby");
                book1.setDescription("A classic American novel by F. Scott Fitzgerald");
                book1.setPrice(new BigDecimal("15.99"));
                book1.setStockQuantity(25);
                book1.setStatus(Item.ItemStatus.ACTIVE);
                itemRepository.save(book1);

                Item book2 = new Item();
                book2.setName("To Kill a Mockingbird");
                book2.setDescription("A gripping tale of racial injustice and childhood innocence by Harper Lee");
                book2.setPrice(new BigDecimal("18.50"));
                book2.setStockQuantity(30);
                book2.setStatus(Item.ItemStatus.ACTIVE);
                itemRepository.save(book2);

                Item book3 = new Item();
                book3.setName("1984");
                book3.setDescription("George Orwell's dystopian masterpiece about totalitarian control");
                book3.setPrice(new BigDecimal("14.75"));
                book3.setStockQuantity(20);
                book3.setStatus(Item.ItemStatus.ACTIVE);
                itemRepository.save(book3);

                Item book4 = new Item();
                book4.setName("Pride and Prejudice");
                book4.setDescription("Jane Austen's beloved romance novel");
                book4.setPrice(new BigDecimal("16.99"));
                book4.setStockQuantity(15);
                book4.setStatus(Item.ItemStatus.ACTIVE);
                itemRepository.save(book4);

                Item book5 = new Item();
                book5.setName("The Catcher in the Rye");
                book5.setDescription("J.D. Salinger's coming-of-age story");
                book5.setPrice(new BigDecimal("17.25"));
                book5.setStockQuantity(0);
                book5.setStatus(Item.ItemStatus.OUT_OF_STOCK);
                itemRepository.save(book5);

                Item book6 = new Item();
                book6.setName("Lord of the Flies");
                book6.setDescription("William Golding's tale of survival and human nature");
                book6.setPrice(new BigDecimal("13.99"));
                book6.setStockQuantity(5);
                book6.setStatus(Item.ItemStatus.ACTIVE);
                itemRepository.save(book6);

                Item book7 = new Item();
                book7.setName("Harry Potter and the Sorcerer's Stone");
                book7.setDescription("The first book in J.K. Rowling's magical series");
                book7.setPrice(new BigDecimal("22.99"));
                book7.setStockQuantity(40);
                book7.setStatus(Item.ItemStatus.ACTIVE);
                itemRepository.save(book7);

                Item book8 = new Item();
                book8.setName("The Hobbit");
                book8.setDescription("J.R.R. Tolkien's adventure story");
                book8.setPrice(new BigDecimal("19.99"));
                book8.setStockQuantity(12);
                book8.setStatus(Item.ItemStatus.ACTIVE);
                itemRepository.save(book8);

                System.out.println("Sample items seeded successfully!");
            }
        };
    }
}
