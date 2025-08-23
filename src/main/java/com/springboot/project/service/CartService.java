package com.springboot.project.service;

import com.springboot.project.dto.CartItem;
import com.springboot.project.entity.Item;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;

@Service
@SessionScope
public class CartService {

    private List<CartItem> cartItems = new ArrayList<>();

    public void addItem(Item item, int quantity) {
        for (CartItem cartItem : cartItems) {
            if (cartItem.getItem().getItemCode().equals(item.getItemCode())) {
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                return;
            }
        }
        cartItems.add(new CartItem(item, quantity));
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void clearCart() {
        cartItems.clear();
    }
}
