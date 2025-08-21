package com.springboot.project.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "items")
@Data
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @Column(name = "item_code")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemCode;

    @NotBlank(message = "Item name is required")
    @Column(unique = true)
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 1, message = "Stock quantity must be a positive number")
    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Enumerated(EnumType.STRING)
    private ItemStatus status = ItemStatus.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Item(String name, BigDecimal price, Integer stockQuantity, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.stockQuantity = stockQuantity;
    }

    public enum ItemStatus {
        ACTIVE, INACTIVE, OUT_OF_STOCK
    }
}
