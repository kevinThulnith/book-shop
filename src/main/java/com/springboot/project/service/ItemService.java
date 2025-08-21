package com.springboot.project.service;

import com.springboot.project.entity.Item;
import com.springboot.project.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    // Create new item (Admin only)
    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    // Get all items
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    // Get active items only (for customers)
    public List<Item> getActiveItems() {
        return itemRepository.findActiveItemsOrderByName();
    }

    // Get item by ID
    public Optional<Item> getItemById(Long itemCode) {
        return itemRepository.findById(itemCode);
    }

    // Update item (Admin full update, Staff quantity only)
    public Item updateItem(Item item) {
        return itemRepository.save(item);
    }

    // Update only stock quantity (Staff)
    public Item updateItemQuantity(Long itemCode, Integer newQuantity) {
        Optional<Item> itemOpt = itemRepository.findById(itemCode);
        if (itemOpt.isPresent()) {
            Item item = itemOpt.get();
            item.setStockQuantity(newQuantity);
            
            // Auto update status based on stock
            if (newQuantity == 0) {
                item.setStatus(Item.ItemStatus.OUT_OF_STOCK);
            } else if (item.getStatus() == Item.ItemStatus.OUT_OF_STOCK) {
                item.setStatus(Item.ItemStatus.ACTIVE);
            }
            
            return itemRepository.save(item);
        }
        return null;
    }

    // Delete item (Admin only)
    public void deleteItem(Long itemCode) {
        itemRepository.deleteById(itemCode);
    }

    // Search items by name
    public List<Item> searchItemsByName(String name) {
        return itemRepository.findByNameContainingIgnoreCase(name);
    }

    // Check if item name exists
    public boolean existsByName(String name) {
        return itemRepository.existsByName(name);
    }

    // Check if item name exists excluding current item
    public boolean existsByNameAndNotId(String name, Long itemCode) {
        return itemRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .anyMatch(item -> !item.getItemCode().equals(itemCode) && 
                         item.getName().equalsIgnoreCase(name));
    }
}
