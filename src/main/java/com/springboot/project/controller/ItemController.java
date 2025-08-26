package com.springboot.project.controller;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import com.springboot.project.service.ItemService;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import com.springboot.project.entity.Item;
import com.springboot.project.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import jakarta.validation.Valid;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

@Controller
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    // ADMIN ROUTES - Full CRUD operations
    @GetMapping("/admin")
    public String adminItemList(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.ADMIN) {
            return "redirect:/login";
        }

        List<Item> items = itemService.getAllItems();
        model.addAttribute("items", items);
        model.addAttribute("user", currentUser);
        return "admin-items";
    }

    @GetMapping("/admin/add")
    public String showAddItemForm(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.ADMIN) {
            return "redirect:/login";
        }

        model.addAttribute("item", new Item());
        model.addAttribute("user", currentUser);
        return "add-item";
    }

    @PostMapping("/admin/add")
    public String addItem(@Valid @ModelAttribute Item item, BindingResult result, 
                         HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.ADMIN) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("user", currentUser);
            return "add-item";
        }

        if (itemService.existsByName(item.getName())) {
            result.rejectValue("name", "error.item", "Item with this name already exists");
            model.addAttribute("user", currentUser);
            return "add-item";
        }

        itemService.saveItem(item);
        redirectAttributes.addFlashAttribute("successMessage", "Item added successfully!");
        return "redirect:/items/admin";
    }

    @GetMapping("/admin/edit/{itemCode}")
    public String showEditItemForm(@PathVariable Long itemCode, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.ADMIN) {
            return "redirect:/login";
        }

        Optional<Item> itemOpt = itemService.getItemById(itemCode);
        if (itemOpt.isPresent()) {
            model.addAttribute("item", itemOpt.get());
            model.addAttribute("user", currentUser);
            return "edit-item";
        }

        return "redirect:/items/admin";
    }

    @PostMapping("/admin/edit/{itemCode}")
    public String editItem(@PathVariable Long itemCode, @Valid @ModelAttribute Item item, 
                          BindingResult result, HttpSession session, Model model, 
                          RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.ADMIN) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("user", currentUser);
            return "edit-item";
        }

        if (itemService.existsByNameAndNotId(item.getName(), itemCode)) {
            result.rejectValue("name", "error.item", "Item with this name already exists");
            model.addAttribute("user", currentUser);
            return "edit-item";
        }

        item.setItemCode(itemCode);
        itemService.updateItem(item);
        redirectAttributes.addFlashAttribute("successMessage", "Item updated successfully!");
        return "redirect:/items/admin";
    }

    @PostMapping("/admin/delete/{itemCode}")
    public String deleteItem(@PathVariable Long itemCode, HttpSession session, 
                           RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.ADMIN) {
            return "redirect:/login";
        }

        itemService.deleteItem(itemCode);
        redirectAttributes.addFlashAttribute("successMessage", "Item deleted successfully!");
        return "redirect:/items/admin";
    }

    // STAFF ROUTES - List all and update quantity only
    @GetMapping("/staff")
    public String staffItemList(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.STAFF) {
            return "redirect:/login";
        }

        List<Item> items = itemService.getAllItems();
        model.addAttribute("items", items);
        model.addAttribute("user", currentUser);
        return "staff-items";
    }

    @PostMapping("/staff/update-quantity/{itemCode}")
    public String updateItemQuantity(@PathVariable Long itemCode, 
                                   @RequestParam Integer stockQuantity,
                                   HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.STAFF) {
            return "redirect:/login";
        }

        Item updatedItem = itemService.updateItemQuantity(itemCode, stockQuantity);
        if (updatedItem != null) {
            redirectAttributes.addFlashAttribute("successMessage", "Stock quantity updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Item not found!");
        }

        return "redirect:/items/staff";
    }

    // CUSTOMER ROUTES - View list and item details
    @GetMapping("/customer")
    public String customerItemList(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.CUSTOMER) {
            return "redirect:/login";
        }

        List<Item> items = itemService.getActiveItems();
        model.addAttribute("items", items);
        model.addAttribute("user", currentUser);

        if (!items.isEmpty()) {
            BigDecimal totalPrice = items.stream()
                .filter(item -> item.getStatus() == Item.ItemStatus.ACTIVE)
                .map(Item::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avaragePrice = totalPrice.divide(
                BigDecimal.valueOf(items.size()),
                2,
                RoundingMode.HALF_UP
            );
            model.addAttribute("averagePrice", avaragePrice);
        } else {
            model.addAttribute("averagePrice", BigDecimal.ZERO);
        }

        return "customer-items";
    }

    @GetMapping("/customer/details/{itemCode}")
    public String itemDetails(@PathVariable Long itemCode, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.CUSTOMER) {
            return "redirect:/login";
        }

        Optional<Item> itemOpt = itemService.getItemById(itemCode);
        if (itemOpt.isPresent() && itemOpt.get().getStatus() == Item.ItemStatus.ACTIVE) {
            model.addAttribute("item", itemOpt.get());
            model.addAttribute("user", currentUser);
            return "item-details";
        }

        return "redirect:/items/customer";
    }
}
