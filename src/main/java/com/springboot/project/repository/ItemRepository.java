package com.springboot.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.springboot.project.entity.Item;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    // Basic finder methods
    Optional<Item> findByName(String name);
    Optional<Item> findByItemCode(Long itemCode);
    
    // Existence checks
    boolean existsByName(String name);
    boolean existsByItemCode(Long itemCode);
    
    // Find by status
    List<Item> findByStatus(Item.ItemStatus status);
    
    // Search items by name (case insensitive)
    List<Item> findByNameContainingIgnoreCase(String name);
    
    // Search items by description (case insensitive)
    List<Item> findByDescriptionContainingIgnoreCase(String description);
    
    // Find items by price range
    List<Item> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<Item> findByPriceGreaterThan(BigDecimal price);
    List<Item> findByPriceLessThan(BigDecimal price);
    
    // Find items by stock quantity
    List<Item> findByStockQuantityGreaterThan(Integer quantity);
    List<Item> findByStockQuantityLessThan(Integer quantity);
    List<Item> findByStockQuantityBetween(Integer minQuantity, Integer maxQuantity);
    
    // Find low stock items
    List<Item> findByStockQuantityLessThanAndStatus(Integer threshold, Item.ItemStatus status);
    
    // Find items created within date range
    List<Item> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find items by status and stock quantity
    List<Item> findByStatusAndStockQuantityGreaterThan(Item.ItemStatus status, Integer quantity);
    
    // Find items by multiple statuses
    List<Item> findByStatusIn(List<Item.ItemStatus> statuses);
    
    // Custom queries using @Query annotation
    
    // Find items with zero stock
    @Query("SELECT i FROM Item i WHERE i.stockQuantity = 0")
    List<Item> getOutOfStockItems();
    
    // Find items that need restocking (below threshold)
    @Query("SELECT i FROM Item i WHERE i.stockQuantity <= :threshold AND i.status = :status")
    List<Item> getItemsNeedingRestock(@Param("threshold") Integer threshold, @Param("status") Item.ItemStatus status);
    
    // Get total inventory value
    @Query("SELECT SUM(i.price * i.stockQuantity) FROM Item i WHERE i.status = :status")
    BigDecimal getTotalInventoryValue(@Param("status") Item.ItemStatus status);
    
    // Get average item price
    @Query("SELECT AVG(i.price) FROM Item i WHERE i.status = :status")
    BigDecimal getAverageItemPrice(@Param("status") Item.ItemStatus status);
    
    // Count items by status
    @Query("SELECT COUNT(i) FROM Item i WHERE i.status = :status")
    Long countItemsByStatus(@Param("status") Item.ItemStatus status);
    
    // Find most expensive items
    @Query("SELECT i FROM Item i WHERE i.status = :status ORDER BY i.price DESC")
    List<Item> getMostExpensiveItems(@Param("status") Item.ItemStatus status);
    
    // Find cheapest items
    @Query("SELECT i FROM Item i WHERE i.status = :status ORDER BY i.price ASC")
    List<Item> getCheapestItems(@Param("status") Item.ItemStatus status);
    
    // Find recently added items
    @Query("SELECT i FROM Item i WHERE i.createdAt >= :dateThreshold ORDER BY i.createdAt DESC")
    List<Item> getRecentlyAddedItems(@Param("dateThreshold") LocalDateTime dateThreshold);
    
    // Get inventory statistics
    @Query("SELECT i.status, COUNT(i) as itemCount, " +
           "SUM(i.stockQuantity) as totalStock, " +
           "AVG(i.price) as averagePrice, " +
           "SUM(i.price * i.stockQuantity) as totalValue " +
           "FROM Item i GROUP BY i.status")
    List<Object[]> getInventoryStatistics();
    
    // Search items by multiple criteria
    @Query("SELECT i FROM Item i WHERE " +
           "(:name IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:description IS NULL OR LOWER(i.description) LIKE LOWER(CONCAT('%', :description, '%'))) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:minPrice IS NULL OR i.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR i.price <= :maxPrice) AND " +
           "(:minStock IS NULL OR i.stockQuantity >= :minStock)")
    List<Item> searchItems(@Param("name") String name,
                          @Param("description") String description,
                          @Param("status") Item.ItemStatus status,
                          @Param("minPrice") BigDecimal minPrice,
                          @Param("maxPrice") BigDecimal maxPrice,
                          @Param("minStock") Integer minStock);
    
    // Get items by price range and status
    @Query("SELECT i FROM Item i WHERE i.status = :status AND i.price BETWEEN :minPrice AND :maxPrice ORDER BY i.price")
    List<Item> getItemsByPriceRangeAndStatus(@Param("status") Item.ItemStatus status,
                                            @Param("minPrice") BigDecimal minPrice,
                                            @Param("maxPrice") BigDecimal maxPrice);
    
    // Update item stock quantity
    @Modifying
    @Query("UPDATE Item i SET i.stockQuantity = :newQuantity WHERE i.itemCode = :itemCode")
    int updateItemStock(@Param("itemCode") Long itemCode, @Param("newQuantity") Integer newQuantity);
    
    // Update item price
    @Modifying
    @Query("UPDATE Item i SET i.price = :newPrice WHERE i.itemCode = :itemCode")
    int updateItemPrice(@Param("itemCode") Long itemCode, @Param("newPrice") BigDecimal newPrice);
    
    // Update item status
    @Modifying
    @Query("UPDATE Item i SET i.status = :newStatus WHERE i.itemCode = :itemCode")
    int updateItemStatus(@Param("itemCode") Long itemCode, @Param("newStatus") Item.ItemStatus newStatus);
    
    // Bulk update status for out of stock items
    @Modifying
    @Query("UPDATE Item i SET i.status = :newStatus WHERE i.stockQuantity = 0 AND i.status != :newStatus")
    int updateOutOfStockItemsStatus(@Param("newStatus") Item.ItemStatus newStatus);
    
    // Increase stock quantity
    @Modifying
    @Query("UPDATE Item i SET i.stockQuantity = i.stockQuantity + :quantity WHERE i.itemCode = :itemCode")
    int increaseItemStock(@Param("itemCode") Long itemCode, @Param("quantity") Integer quantity);
    
    // Decrease stock quantity
    @Modifying
    @Query("UPDATE Item i SET i.stockQuantity = i.stockQuantity - :quantity WHERE i.itemCode = :itemCode AND i.stockQuantity >= :quantity")
    int decreaseItemStock(@Param("itemCode") Long itemCode, @Param("quantity") Integer quantity);
    
    // Get low stock alert items
    @Query("SELECT i FROM Item i WHERE i.stockQuantity <= :threshold AND i.status = 'ACTIVE' ORDER BY i.stockQuantity ASC")
    List<Item> getLowStockAlertItems(@Param("threshold") Integer threshold);
    
    // Get items created in last N days
    @Query("SELECT i FROM Item i WHERE i.createdAt >= :dateThreshold AND i.status = :status ORDER BY i.createdAt DESC")
    List<Item> getItemsCreatedSince(@Param("dateThreshold") LocalDateTime dateThreshold, @Param("status") Item.ItemStatus status);
    
    // Get total items count by date range
    @Query("SELECT COUNT(i) FROM Item i WHERE i.createdAt BETWEEN :startDate AND :endDate")
    Long getItemsCountByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find items with name or description containing keyword
    @Query("SELECT i FROM Item i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Item> searchByKeyword(@Param("keyword") String keyword);
}
