package com.springboot.project.repository;

import com.springboot.project.entity.CartItem;
import com.springboot.project.entity.Cart;
import com.springboot.project.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndItem(Cart cart, Item item);
    void deleteByCartAndItem(Cart cart, Item item);
}
