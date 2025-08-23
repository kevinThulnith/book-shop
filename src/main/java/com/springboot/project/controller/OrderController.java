package com.springboot.project.controller;

import com.springboot.project.service.OrderService;
import com.springboot.project.service.UserService;
import com.springboot.project.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @GetMapping
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "orders";
    }

    @GetMapping("/new")
    public String showOrderForm(Model model) {
        model.addAttribute("customers", userService.getAllCustomers());
        model.addAttribute("items", itemService.getActiveItems());
        return "create_order";
    }

    @PostMapping
    public String createOrder(@RequestParam("customerId") Long customerId,
                              @RequestParam Map<String, String> itemQuantities) {
        // The itemQuantities map will contain all form parameters.
        // We need to filter it to get only the item quantities.
        java.util.Map<Long, Integer> items = new java.util.HashMap<>();
        for (Map.Entry<String, String> entry : itemQuantities.entrySet()) {
            if (entry.getKey().startsWith("items[")) {
                try {
                    Long itemId = Long.parseLong(entry.getKey().substring(6, entry.getKey().length() - 1));
                    int quantity = Integer.parseInt(entry.getValue());
                    if (quantity > 0) {
                        items.put(itemId, quantity);
                    }
                } catch (NumberFormatException e) {
                    // Ignore if the key is not a valid long or value is not a valid int
                }
            }
        }
        orderService.createOrder(customerId, items);
        return "redirect:/orders";
    }
}
