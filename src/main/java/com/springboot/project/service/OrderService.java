package com.springboot.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.springboot.project.entity.*;
import com.springboot.project.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
@Transactional
public class OrderService {
    
    @Autowired
    private BillRepository billRepository;
    
    @Autowired
    private BillItemRepository billItemRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ItemRepository itemRepository;
    
    // Create a new order (bill) for a customer
    public Bill createOrder(Long customerId) {
        Optional<User> customerOpt = userRepository.findById(customerId);
        if (customerOpt.isPresent()) {
            User customer = customerOpt.get();
            Bill bill = new Bill(customer);
            bill.setStatus(Bill.BillStatus.DRAFT);
            return billRepository.save(bill);
        }
        throw new RuntimeException("Customer not found with ID: " + customerId);
    }
    
    // Add an item to an existing order
    public BillItem addItemToOrder(Long billId, Long itemId, Integer quantity) {
        Optional<Bill> billOpt = billRepository.findById(billId);
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        
        if (billOpt.isPresent() && itemOpt.isPresent()) {
            Bill bill = billOpt.get();
            Item item = itemOpt.get();
            
            // Check if item has sufficient stock
            if (item.getStockQuantity() < quantity) {
                throw new RuntimeException("Insufficient stock for item: " + item.getName() + 
                    ". Available: " + item.getStockQuantity() + ", Requested: " + quantity);
            }
            
            // Initialize billItems list if null (shouldn't happen with our fix, but safety check)
            if (bill.getBillItems() == null) {
                bill.setBillItems(new ArrayList<>());
            }
            
            // Check if item is already in the bill
            Optional<BillItem> existingBillItem = bill.getBillItems().stream()
                .filter(bi -> bi.getItem().getItemCode().equals(itemId))
                .findFirst();
            
            if (existingBillItem.isPresent()) {
                // Update existing item quantity
                BillItem billItem = existingBillItem.get();
                int newQuantity = billItem.getQuantity() + quantity;
                
                if (item.getStockQuantity() < newQuantity) {
                    throw new RuntimeException("Insufficient stock for item: " + item.getName() + 
                        ". Available: " + item.getStockQuantity() + ", Total Requested: " + newQuantity);
                }
                
                billItem.setQuantity(newQuantity);
                return billItemRepository.save(billItem);
            } else {
                // Create new bill item
                BillItem billItem = new BillItem(bill, item, quantity, item.getPrice());
                BillItem savedBillItem = billItemRepository.save(billItem);
                // Add to the bill's list to keep it in sync
                bill.getBillItems().add(savedBillItem);
                return savedBillItem;
            }
        }
        throw new RuntimeException("Bill or Item not found");
    }
    
    // Remove an item from an order
    public void removeItemFromOrder(Long billItemId) {
        billItemRepository.deleteById(billItemId);
    }
    
    // Update item quantity in an order
    public BillItem updateItemQuantity(Long billItemId, Integer quantity) {
        Optional<BillItem> billItemOpt = billItemRepository.findById(billItemId);
        
        if (billItemOpt.isPresent()) {
            BillItem billItem = billItemOpt.get();
            Item item = billItem.getItem();
            
            // Check if item has sufficient stock
            if (item.getStockQuantity() < quantity) {
                throw new RuntimeException("Insufficient stock for item: " + item.getName() + 
                    ". Available: " + item.getStockQuantity() + ", Requested: " + quantity);
            }
            
            billItem.setQuantity(quantity);
            return billItemRepository.save(billItem);
        }
        throw new RuntimeException("Bill item not found");
    }
    
    // Calculate and save the order totals
    public Bill calculateOrderTotals(Long billId) {
        Optional<Bill> billOpt = billRepository.findById(billId);
        
        if (billOpt.isPresent()) {
            Bill bill = billOpt.get();
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            // Initialize billItems list if null and fetch from repository
            if (bill.getBillItems() == null) {
                bill.setBillItems(billItemRepository.findByBill(bill));
            }
            
            if (bill.getBillItems() != null && !bill.getBillItems().isEmpty()) {
                for (BillItem billItem : bill.getBillItems()) {
                    if (billItem.getTotalPrice() != null) {
                        totalAmount = totalAmount.add(billItem.getTotalPrice());
                    }
                }
            }
            
            bill.setTotalAmount(totalAmount);
            
            // Calculate tax (assuming 10% tax rate)
            BigDecimal taxAmount = totalAmount.multiply(BigDecimal.valueOf(0.10));
            bill.setTaxAmount(taxAmount);
            
            // Calculate final amount
            BigDecimal finalAmount = totalAmount.add(taxAmount).subtract(bill.getDiscountAmount());
            bill.setFinalAmount(finalAmount);
            
            return billRepository.save(bill);
        }
        throw new RuntimeException("Bill not found");
    }
    
    // Confirm the order and reduce stock quantities
    public Bill confirmOrder(Long billId) {
        Optional<Bill> billOpt = billRepository.findById(billId);
        
        if (billOpt.isPresent()) {
            Bill bill = billOpt.get();
            
            // Initialize billItems list if null and fetch from repository
            if (bill.getBillItems() == null) {
                bill.setBillItems(billItemRepository.findByBill(bill));
            }
            
            // Check if there are any items in the order
            if (bill.getBillItems() == null || bill.getBillItems().isEmpty()) {
                throw new RuntimeException("Cannot confirm an order with no items.");
            }
            
            // Check stock availability one more time before confirming
            for (BillItem billItem : bill.getBillItems()) {
                Item item = billItem.getItem();
                if (item.getStockQuantity() < billItem.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for item: " + item.getName() + 
                        ". Available: " + item.getStockQuantity() + ", Required: " + billItem.getQuantity());
                }
            }
            
            // Reduce stock quantities
            for (BillItem billItem : bill.getBillItems()) {
                Item item = billItem.getItem();
                item.setStockQuantity(item.getStockQuantity() - billItem.getQuantity());
                
                // Update item status if out of stock
                if (item.getStockQuantity() == 0) {
                    item.setStatus(Item.ItemStatus.OUT_OF_STOCK);
                }
                
                itemRepository.save(item);
            }
            
            // Calculate totals
            calculateOrderTotals(billId);
            
            // Update bill status
            bill.setStatus(Bill.BillStatus.CONFIRMED);
            bill.setPaidAt(LocalDateTime.now());
            
            return billRepository.save(bill);
        }
        throw new RuntimeException("Bill not found");
    }
    
    // Get all orders
    public List<Bill> getAllOrders() {
        return billRepository.findAllByOrderByCreatedAtDesc();
    }
    
    // Get orders by customer
    public List<Bill> getOrdersByCustomer(Long customerId) {
        return billRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }
    
    // Get order by ID
    public Optional<Bill> getOrderById(Long billId) {
        return billRepository.findById(billId);
    }
    
    // Get all customers (for dropdown selection)
    public List<User> getAllCustomers() {
        return userRepository.findByType(User.userType.CUSTOMER);
    }
    
    // Get all active items (for selection)
    public List<Item> getActiveItems() {
        return itemRepository.findActiveItemsOrderByName();
    }
    
    // Create order from cart (for checkout)
    public Bill createOrderFromCart(Bill order) {
        // Save the order and its items
        Bill savedOrder = billRepository.save(order);
        
        // Save all bill items
        for (BillItem billItem : order.getBillItems()) {
            billItem.setBill(savedOrder);
            billItemRepository.save(billItem);
        }
        
        // Calculate and set totals
        calculateOrderTotals(savedOrder.getBillNumber());
        
        return billRepository.findById(savedOrder.getBillNumber()).orElse(savedOrder);
    }
}
