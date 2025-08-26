package com.springboot.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.springboot.project.entity.Item;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByStatus(Item.ItemStatus status);    
    List<Item> findByNameContainingIgnoreCase(String name);
    boolean existsByName(String name);

    @Query("SELECT i FROM Item i WHERE i.status = 'ACTIVE' ORDER BY i.name")
    List<Item> findActiveItemsOrderByName();
}
