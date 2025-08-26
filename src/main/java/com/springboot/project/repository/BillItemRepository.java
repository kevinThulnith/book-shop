package com.springboot.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.springboot.project.entity.BillItem;
import com.springboot.project.entity.Bill;
import com.springboot.project.entity.Item;
import java.util.List;

@Repository
public interface BillItemRepository extends JpaRepository<BillItem, Long> {
    List<BillItem> findByBill(Bill bill);
    List<BillItem> findByItem(Item item);
    void deleteByBill(Bill bill);
}
