package com.springboot.project.controller;

import com.springboot.project.entity.*;
import com.springboot.project.service.CartService;
import com.springboot.project.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    // View cart
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.CUSTOMER) {
            return "redirect:/login";
        }

        Cart cart = cartService.getCart(currentUser);
        model.addAttribute("cart", cart);
        model.addAttribute("user", currentUser);
        
        return "cart";
    }

    // Add item to cart
    @PostMapping("/add/{itemCode}")
    public String addItemToCart(@PathVariable Long itemCode,
                               @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.CUSTOMER) {
            return "redirect:/login";
        }

        try {
            cartService.addItemToCart(currentUser, itemCode, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Item added to cart successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/items/customer";
    }

    // Add item to cart from item details page
    @PostMapping("/add-from-details")
    public String addItemToCartFromDetails(@RequestParam("itemCode") Long itemCode,
                                          @RequestParam("quantity") Integer quantity,
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.CUSTOMER) {
            return "redirect:/login";
        }

        try {
            cartService.addItemToCart(currentUser, itemCode, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Item added to cart successfully!");
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/items/customer/details/" + itemCode;
        }
    }

    // Update cart item quantity
    @PostMapping("/update/{cartItemId}")
    public String updateCartItemQuantity(@PathVariable Long cartItemId,
                                        @RequestParam("quantity") Integer quantity,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.CUSTOMER) {
            return "redirect:/login";
        }

        try {
            cartService.updateCartItemQuantity(currentUser, cartItemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cart updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    // Remove item from cart
    @PostMapping("/remove/{cartItemId}")
    public String removeItemFromCart(@PathVariable Long cartItemId,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.CUSTOMER) {
            return "redirect:/login";
        }

        try {
            cartService.removeItemFromCart(currentUser, cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    // Clear entire cart
    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.CUSTOMER) {
            return "redirect:/login";
        }

        try {
            cartService.clearCart(currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Cart cleared successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    // Checkout - convert cart to order
    @PostMapping("/checkout")
    public String checkout(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.CUSTOMER) {
            return "redirect:/login";
        }

        try {
            Bill order = cartService.convertCartToOrder(currentUser);
            Bill savedOrder = orderService.createOrderFromCart(order);
            
            // Clear cart after successful order creation
            cartService.clearCart(currentUser);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Order created successfully! Order Number: " + savedOrder.getBillNumber());
            return "redirect:/customer/orders/" + savedOrder.getBillNumber();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }
}
