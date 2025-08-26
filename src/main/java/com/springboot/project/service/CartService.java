package com.springboot.project.service;

import com.springboot.project.entity.*;
import com.springboot.project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    // Get or create cart for customer
    public Cart getOrCreateCart(User customer) {
        Optional<Cart> existingCart = cartRepository.findByCustomer(customer);
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            cart.calculateTotalAmount();
            return cart;
        } else {
            Cart newCart = new Cart(customer);
            return cartRepository.save(newCart);
        }
    }

    // Add item to cart
    public void addItemToCart(User customer, Long itemCode, Integer quantity) {
        Cart cart = getOrCreateCart(customer);
        Optional<Item> itemOpt = itemRepository.findById(itemCode);
        
        if (itemOpt.isEmpty()) {
            throw new RuntimeException("Item not found");
        }
        
        Item item = itemOpt.get();
        
        if (item.getStatus() != Item.ItemStatus.ACTIVE) {
            throw new RuntimeException("Item is not available");
        }
        
        if (item.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + item.getStockQuantity());
        }

        // Check if item already exists in cart
        Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndItem(cart, item);
        
        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;
            
            if (item.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Insufficient stock. Available: " + item.getStockQuantity() + 
                                         ", already in cart: " + cartItem.getQuantity());
            }
            
            cartItem.setQuantity(newQuantity);
            cartItem.updateTotalPrice();
            cartItemRepository.save(cartItem);
        } else {
            CartItem newCartItem = new CartItem(cart, item, quantity);
            cart.getCartItems().add(newCartItem);
            cartItemRepository.save(newCartItem);
        }
        
        cart.calculateTotalAmount();
        cartRepository.save(cart);
    }

    // Update item quantity in cart
    public void updateCartItemQuantity(User customer, Long cartItemId, Integer quantity) {
        Cart cart = getOrCreateCart(customer);
        Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
        
        if (cartItemOpt.isEmpty()) {
            throw new RuntimeException("Cart item not found");
        }
        
        CartItem cartItem = cartItemOpt.get();
        
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to this customer");
        }
        
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }
        
        if (cartItem.getItem().getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + cartItem.getItem().getStockQuantity());
        }
        
        cartItem.setQuantity(quantity);
        cartItem.updateTotalPrice();
        cartItemRepository.save(cartItem);
        
        cart.calculateTotalAmount();
        cartRepository.save(cart);
    }

    // Remove item from cart
    public void removeItemFromCart(User customer, Long cartItemId) {
        Cart cart = getOrCreateCart(customer);
        Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
        
        if (cartItemOpt.isEmpty()) {
            throw new RuntimeException("Cart item not found");
        }
        
        CartItem cartItem = cartItemOpt.get();
        
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to this customer");
        }
        
        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        
        cart.calculateTotalAmount();
        cartRepository.save(cart);
    }

    // Clear entire cart
    public void clearCart(User customer) {
        Optional<Cart> cartOpt = cartRepository.findByCustomer(customer);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cart.getCartItems().clear();
            cartItemRepository.deleteAll(cartItemRepository.findAll().stream()
                .filter(item -> item.getCart().getId().equals(cart.getId()))
                .toList());
            cart.calculateTotalAmount();
            cartRepository.save(cart);
        }
    }

    // Get cart for customer
    public Cart getCart(User customer) {
        return getOrCreateCart(customer);
    }

    // Convert cart to order
    public Bill convertCartToOrder(User customer) {
        Cart cart = getOrCreateCart(customer);
        
        if (cart.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        // Create new bill
        Bill bill = new Bill(customer);
        bill.setStatus(Bill.BillStatus.DRAFT);
        
        // Convert cart items to bill items
        for (CartItem cartItem : cart.getCartItems()) {
            BillItem billItem = new BillItem(
                bill, 
                cartItem.getItem(), 
                cartItem.getQuantity(), 
                cartItem.getUnitPrice()
            );
            bill.getBillItems().add(billItem);
        }
        
        // Calculate bill totals
        bill.setTotalAmount(cart.getTotalAmount());
        bill.setFinalAmount(cart.getTotalAmount());
        
        return bill;
    }
}
