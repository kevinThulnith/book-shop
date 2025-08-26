package com.springboot.project.repository;

import com.springboot.project.entity.Cart;
import com.springboot.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomer(User customer);
    void deleteByCustomer(User customer);
}
