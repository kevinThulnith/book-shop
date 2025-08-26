package com.springboot.project.controller;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import com.springboot.project.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import com.springboot.project.entity.*;
import org.springframework.ui.Model;
import java.util.Optional;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    // Display all orders (for staff and admin)
    @GetMapping
    public String viewOrders(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() == User.userType.CUSTOMER) {
            return "redirect:/login";
        }
        
        List<Bill> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("user", currentUser);
        return "order-list";
    }
    
    // Show create order form
    @GetMapping("/create")
    public String showCreateOrderForm(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() == User.userType.CUSTOMER) {
            return "redirect:/login";
        }
        
        List<User> customers = orderService.getAllCustomers();
        List<Item> items = orderService.getActiveItems();
        
        model.addAttribute("customers", customers);
        model.addAttribute("items", items);
        model.addAttribute("user", currentUser);
        return "create-order";
    }
    
    // Create a new order
    @PostMapping("/create")
    public String createOrder(@RequestParam("customerId") Long customerId, 
                            RedirectAttributes redirectAttributes, 
                            HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() == User.userType.CUSTOMER) {
            return "redirect:/login";
        }
        
        try {
            Bill order = orderService.createOrder(customerId);
            redirectAttributes.addFlashAttribute("successMessage", "Order created successfully!");
            return "redirect:/orders/" + order.getBillNumber() + "/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating order: " + e.getMessage());
            return "redirect:/orders/create";
        }
    }
    
    // Create a new order with items
    @PostMapping("/create-with-items")
    public String createOrderWithItems(@RequestParam("customerId") Long customerId,
                                     @RequestParam(value = "itemIds", required = false) List<Long> itemIds,
                                     @RequestParam Map<String, String> allParams,
                                     RedirectAttributes redirectAttributes,
                                     HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() == User.userType.CUSTOMER) {
            return "redirect:/login";
        }
        
        try {
            // Check if any items were selected
            if (itemIds == null || itemIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one item for the order.");
                return "redirect:/orders/create";
            }
            
            // Create the order first
            Bill order = orderService.createOrder(customerId);
            
            // Add items to the order
            int itemsAdded = 0;
            for (Long itemId : itemIds) {
                String quantityKey = "quantities[" + itemId + "]";
                String quantityStr = allParams.get(quantityKey);
                if (quantityStr != null && !quantityStr.isEmpty()) {
                    try {
                        Integer quantity = Integer.parseInt(quantityStr);
                        if (quantity > 0) {
                            orderService.addItemToOrder(order.getBillNumber(), itemId, quantity);
                            itemsAdded++;
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid quantity values
                    }
                }
            }
            
            if (itemsAdded == 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "No valid items were added to the order.");
                return "redirect:/orders/create";
            }
            
            // Calculate totals
            orderService.calculateOrderTotals(order.getBillNumber());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Order created successfully with " + itemsAdded + " items!");
            return "redirect:/orders/" + order.getBillNumber() + "/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating order: " + e.getMessage());
            return "redirect:/orders/create";
        }
    }
    
    // Show order details and edit form
    @GetMapping("/{orderId}/edit")
    public String editOrder(@PathVariable Long orderId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() == User.userType.CUSTOMER) {
            return "redirect:/login";
        }
        
        Optional<Bill> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isPresent()) {
            Bill order = orderOpt.get();
            List<Item> availableItems = orderService.getActiveItems();
            
            model.addAttribute("order", order);
            model.addAttribute("availableItems", availableItems);
            model.addAttribute("user", currentUser);
            return "edit-order";
        } else {
            return "redirect:/orders";
        }
    }
    
    // Add item to order
    @PostMapping("/{orderId}/add-item")
    public String addItemToOrder(@PathVariable Long orderId,
                               @RequestParam("itemId") Long itemId,
                               @RequestParam("quantity") Integer quantity,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() == User.userType.CUSTOMER) {
            return "redirect:/login";
        }
        
        try {
            orderService.addItemToOrder(orderId, itemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Item added to order successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding item: " + e.getMessage());
        }
        
        return "redirect:/orders/" + orderId + "/edit";
    }
    
    // Update item quantity in order
    @PostMapping("/{orderId}/update-item/{billItemId}")
    public String updateItemQuantity(@PathVariable Long orderId,
                                   @PathVariable Long billItemId,
                                   @RequestParam("quantity") Integer quantity,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() == User.userType.CUSTOMER) {
            return "redirect:/login";
        }
        
        try {
            orderService.updateItemQuantity(billItemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Item quantity updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating quantity: " + e.getMessage());
        }
        
        return "redirect:/orders/" + orderId + "/edit";
    }
    
    // Remove item from order
    @PostMapping("/{orderId}/remove-item/{billItemId}")
    public String removeItemFromOrder(@PathVariable Long orderId,
                                    @PathVariable Long billItemId,
                                    RedirectAttributes redirectAttributes,
                                    HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() == User.userType.CUSTOMER) {
            return "redirect:/login";
        }
        
        try {
            orderService.removeItemFromOrder(billItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed from order successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error removing item: " + e.getMessage());
        }
        
        return "redirect:/orders/" + orderId + "/edit";
    }
    
    // Confirm order (finalize and reduce stock)
    @PostMapping("/{orderId}/confirm")
    public String confirmOrder(@PathVariable Long orderId,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() == User.userType.CUSTOMER) {
            return "redirect:/login";
        }
        
        try {
            Bill confirmedOrder = orderService.confirmOrder(orderId);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Order confirmed successfully! Order Number: " + confirmedOrder.getBillNumber());
            return "redirect:/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error confirming order: " + e.getMessage());
            return "redirect:/orders/" + orderId + "/edit";
        }
    }
    
    // View order details (read-only)
    @GetMapping("/{orderId}")
    public String viewOrder(@PathVariable Long orderId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Bill> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isPresent()) {
            Bill order = orderOpt.get();
            model.addAttribute("order", order);
            model.addAttribute("user", currentUser);
            return "order-details";
        } else {
            return "redirect:/orders";
        }
    }
}
