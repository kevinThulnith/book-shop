package com.springboot.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import com.springboot.project.entity.Bill;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

public interface BillRepository extends JpaRepository<Bill, Long> {
    
    // Basic finder methods
    List<Bill> findByStatus(Bill.BillStatus status);
    List<Bill> findByCustomer_AccountNumber(Long customerId);
    
    // Find bills by customer with status
    List<Bill> findByCustomer_AccountNumberAndStatus(Long customerId, Bill.BillStatus status);
    
    // Find bills by date range
    List<Bill> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find bills by date range and status
    List<Bill> findByCreatedAtBetweenAndStatus(LocalDateTime startDate, LocalDateTime endDate, Bill.BillStatus status);
    
    // Find bills by customer and date range
    List<Bill> findByCustomer_AccountNumberAndCreatedAtBetween(Long customerId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find bills with final amount greater than specified amount
    List<Bill> findByFinalAmountGreaterThan(BigDecimal amount);
    
    // Find bills with final amount between range
    List<Bill> findByFinalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    // Find bills by customer name (case insensitive)
    List<Bill> findByCustomer_NameContainingIgnoreCase(String customerName);
    
    // Find paid bills by payment date range
    List<Bill> findByStatusAndPaidAtBetween(Bill.BillStatus status, LocalDateTime startDate, LocalDateTime endDate);
    
    // Custom queries using @Query annotation
    
    // Get total revenue for a specific period
    @Query("SELECT SUM(b.finalAmount) FROM Bill b WHERE b.status = :status AND b.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueByStatusAndDateRange(@Param("status") Bill.BillStatus status, 
                                                   @Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    // Get total revenue for a customer
    @Query("SELECT SUM(b.finalAmount) FROM Bill b WHERE b.customer.accountNumber = :customerId AND b.status = :status")
    BigDecimal getTotalRevenueByCustomer(@Param("customerId") Long customerId, @Param("status") Bill.BillStatus status);
    
    // Count bills by status
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.status = :status")
    Long countBillsByStatus(@Param("status") Bill.BillStatus status);
    
    // Get bills with their items count
    @Query("SELECT b, SIZE(b.billItems) as itemCount FROM Bill b WHERE b.customer.accountNumber = :customerId")
    List<Object[]> getBillsWithItemCount(@Param("customerId") Long customerId);
    
    // Find top customers by total purchase amount
    @Query("SELECT b.customer, SUM(b.finalAmount) as totalAmount FROM Bill b " +
           "WHERE b.status = :status " +
           "GROUP BY b.customer " +
           "ORDER BY totalAmount DESC")
    List<Object[]> getTopCustomersByTotalAmount(@Param("status") Bill.BillStatus status);
    
    // Get monthly revenue report
    @Query("SELECT YEAR(b.createdAt) as year, MONTH(b.createdAt) as month, SUM(b.finalAmount) as totalRevenue " +
           "FROM Bill b WHERE b.status = :status " +
           "GROUP BY YEAR(b.createdAt), MONTH(b.createdAt) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyRevenueReport(@Param("status") Bill.BillStatus status);
    
    // Get bills with high discount percentage
    @Query("SELECT b FROM Bill b WHERE (b.discountAmount / b.totalAmount) * 100 > :discountPercentage")
    List<Bill> getBillsWithHighDiscount(@Param("discountPercentage") Double discountPercentage);
    
    // Get unpaid bills older than specified days
    @Query("SELECT b FROM Bill b WHERE b.status IN (:statuses) AND b.createdAt < :dateThreshold")
    List<Bill> getOverdueBills(@Param("statuses") List<Bill.BillStatus> statuses, 
                               @Param("dateThreshold") LocalDateTime dateThreshold);
    
    // Update bill status
    @Modifying
    @Query("UPDATE Bill b SET b.status = :newStatus, b.paidAt = :paidAt WHERE b.billNumber = :billNumber")
    int updateBillStatus(@Param("billNumber") Long billNumber, 
                         @Param("newStatus") Bill.BillStatus newStatus, 
                         @Param("paidAt") LocalDateTime paidAt);
    
    // Get average bill amount by customer type
    @Query("SELECT b.customer.type, AVG(b.finalAmount) FROM Bill b " +
           "WHERE b.status = :status " +
           "GROUP BY b.customer.type")
    List<Object[]> getAverageBillAmountByCustomerType(@Param("status") Bill.BillStatus status);
    
    // Find bills by multiple statuses
    List<Bill> findByStatusIn(List<Bill.BillStatus> statuses);
    
    // Find recent bills (last N days)
    @Query("SELECT b FROM Bill b WHERE b.createdAt >= :dateThreshold ORDER BY b.createdAt DESC")
    List<Bill> getRecentBills(@Param("dateThreshold") LocalDateTime dateThreshold);
    
    // Get customer's last bill
    @Query("SELECT b FROM Bill b WHERE b.customer.accountNumber = :customerId ORDER BY b.createdAt DESC")
    List<Bill> getCustomerLastBill(@Param("customerId") Long customerId);
    
    // Get bills count by date range
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    Long getBillsCountByDateRange(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);
}
