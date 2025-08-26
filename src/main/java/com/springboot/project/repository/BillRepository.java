package com.springboot.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.springboot.project.entity.Bill;
import com.springboot.project.entity.User;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByCustomer(User customer);
    List<Bill> findByStatus(Bill.BillStatus status);
    List<Bill> findByCustomerOrderByCreatedAtDesc(User customer);
    List<Bill> findAllByOrderByCreatedAtDesc();
    
    @Query("SELECT b FROM Bill b WHERE b.customer.accountNumber = :customerId ORDER BY b.createdAt DESC")
    List<Bill> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId);
    
    @Query("SELECT b FROM Bill b WHERE b.status = :status ORDER BY b.createdAt DESC")
    List<Bill> findByStatusOrderByCreatedAtDesc(@Param("status") Bill.BillStatus status);
}
