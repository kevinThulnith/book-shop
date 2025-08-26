package com.springboot.project.controller;

import com.springboot.project.entity.*;
import com.springboot.project.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class CustomerOrderController {

    @Autowired
    private OrderService orderService;

    // View customer's orders
    @GetMapping("/orders")
    public String viewCustomerOrders(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.CUSTOMER) {
            return "redirect:/login";
        }

        List<Bill> orders = orderService.getOrdersByCustomer(currentUser.getAccountNumber());
        model.addAttribute("orders", orders);
        model.addAttribute("user", currentUser);
        
        return "customer-orders";
    }

    // View specific order details
    @GetMapping("/orders/{orderId}")
    public String viewOrderDetails(@PathVariable Long orderId, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.CUSTOMER) {
            return "redirect:/login";
        }

        Optional<Bill> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isPresent()) {
            Bill order = orderOpt.get();
            
            // Ensure the order belongs to the current customer
            if (!order.getCustomer().getAccountNumber().equals(currentUser.getAccountNumber())) {
                return "redirect:/customer/orders";
            }
            
            model.addAttribute("order", order);
            model.addAttribute("user", currentUser);
            return "customer-order-details";
        } else {
            return "redirect:/customer/orders";
        }
    }
}
