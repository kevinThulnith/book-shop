package com.springboot.project.controller;

import com.springboot.project.entity.Item;
import com.springboot.project.entity.User;
import com.springboot.project.service.CartService;
import com.springboot.project.service.ItemService;
import com.springboot.project.service.OrderService;
import com.springboot.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ItemService itemService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @PostMapping("/add")
    public String addToCart(@RequestParam("itemId") Long itemId, @RequestParam("quantity") int quantity) {
        Optional<Item> itemOptional = itemService.getItemById(itemId);
        if (itemOptional.isPresent()) {
            cartService.addItem(itemOptional.get(), quantity);
        }
        return "redirect:/cart";
    }

    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("cartItems", cartService.getCartItems());
        return "cart";
    }

    @PostMapping("/order")
    public String placeOrder(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> customerOptional = userService.findByUsername(userDetails.getUsername());
        if (customerOptional.isPresent()) {
            orderService.placeOrder(customerOptional.get(), cartService);
            return "redirect:/orders";
        }
        return "redirect:/login";
    }
}
