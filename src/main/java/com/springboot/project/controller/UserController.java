package com.springboot.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.project.entity.User;
import com.springboot.project.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Admin: List all users by type
    @GetMapping("/admin/customers")
    public String listCustomers(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.ADMIN) {
            return "redirect:/login";
        }
        
        List<User> customers = userService.getAllCustomers();
        model.addAttribute("users", customers);
        model.addAttribute("userType", "Customers");
        model.addAttribute("currentUser", currentUser);
        return "user-list";
    }

    @GetMapping("/admin/staff")
    public String listStaff(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.ADMIN) {
            return "redirect:/login";
        }
        
        List<User> staff = userService.getAllStaff();
        model.addAttribute("users", staff);
        model.addAttribute("userType", "Staff");
        model.addAttribute("currentUser", currentUser);
        return "user-list";
    }

    @GetMapping("/admin/admins")
    public String listAdmins(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.ADMIN) {
            return "redirect:/login";
        }
        
        List<User> admins = userService.getAllAdmins();
        model.addAttribute("users", admins);
        model.addAttribute("userType", "Administrators");
        model.addAttribute("currentUser", currentUser);
        return "user-list";
    }

    // Admin: Add new user form
    @GetMapping("/admin/add")
    public String addUserForm(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.ADMIN) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", new User());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userTypes", User.userType.values());
        return "add-user";
    }

    // Admin: Process new user creation
    @PostMapping("/admin/add")
    public String addUser(@Valid @ModelAttribute User user,
                         BindingResult bindingResult,
                         @RequestParam("userType") String userTypeStr,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.ADMIN) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("userTypes", User.userType.values());
            return "add-user";
        }

        try {
            User.userType userType = User.userType.valueOf(userTypeStr);
            userService.createUser(user, userType);
            redirectAttributes.addFlashAttribute("success", 
                "User created successfully as " + userType.toString().toLowerCase() + "!");
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("userTypes", User.userType.values());
            return "add-user";
        }
    }

    // Staff: List customers only
    @GetMapping("/staff/customers")
    public String staffListCustomers(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getType() != User.userType.STAFF) {
            return "redirect:/login";
        }
        
        List<User> customers = userService.getAllCustomers();
        model.addAttribute("users", customers);
        model.addAttribute("userType", "Customers");
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isStaff", true);
        return "user-list";
    }
    
    // Profile update for logged-in user
    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", currentUser);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isOwnProfile", true);
        return "edit-user";
    }
    
    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute User user,
                               BindingResult bindingResult,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isOwnProfile", true);
            return "edit-user";
        }
        
        try {
            User updatedUser = userService.updateProfile(currentUser, user);
            session.setAttribute("user", updatedUser); // Update session with new data
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return getDashboardRedirect(updatedUser.getType());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isOwnProfile", true);
            return "edit-user";
        }
    }
    
    // Edit user form - Admin can edit any user, Staff can edit customers
    @GetMapping("/edit/{accountNumber}")
    public String editUserForm(@PathVariable Long accountNumber, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Check permissions
        if (currentUser.getType() == User.userType.CUSTOMER) {
            return "redirect:/customer/dashboard";
        }
        
        try {
            User userToEdit = userService.findByAccountNumber(accountNumber);
            
            // Staff can only edit customers
            if (currentUser.getType() == User.userType.STAFF && userToEdit.getType() != User.userType.CUSTOMER) {
                redirectAttributes.addFlashAttribute("error", "You can only edit customer profiles!");
                return "redirect:/users/staff/customers";
            }
            
            model.addAttribute("user", userToEdit);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("userTypes", User.userType.values());
            model.addAttribute("isOwnProfile", false);
            return "edit-user";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return getListRedirect(currentUser.getType());
        }
    }
    
    @PostMapping("/edit/{accountNumber}")
    public String updateUser(@PathVariable Long accountNumber,
                           @Valid @ModelAttribute User user,
                           @RequestParam(value = "userType", required = false) String userTypeStr,
                           BindingResult bindingResult,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Check permissions
        if (currentUser.getType() == User.userType.CUSTOMER) {
            return "redirect:/customer/dashboard";
        }
        
        if (bindingResult.hasErrors()) {
            try {
                User userToEdit = userService.findByAccountNumber(accountNumber);
                model.addAttribute("user", userToEdit);
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("userTypes", User.userType.values());
                model.addAttribute("isOwnProfile", false);
                return "edit-user";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                return getListRedirect(currentUser.getType());
            }
        }
        
        try {
            if (currentUser.getType() == User.userType.ADMIN) {
                User.userType newType = userTypeStr != null ? User.userType.valueOf(userTypeStr) : null;
                if (newType == null) {
                    throw new RuntimeException("User type is required!");
                }
                userService.updateUserAsAdmin(accountNumber, user, newType);
                redirectAttributes.addFlashAttribute("success", "User updated successfully!");
            } else if (currentUser.getType() == User.userType.STAFF) {
                userService.updateUserAsStaff(accountNumber, user);
                redirectAttributes.addFlashAttribute("success", "Customer updated successfully!");
            }
            
            return getListRedirect(currentUser.getType());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return getListRedirect(currentUser.getType());
        }
    }
    
    // Helper methods
    private String getDashboardRedirect(User.userType userType) {
        switch (userType) {
            case ADMIN:
                return "redirect:/admin/dashboard";
            case STAFF:
                return "redirect:/staff/dashboard";
            case CUSTOMER:
                return "redirect:/customer/dashboard";
            default:
                return "redirect:/login";
        }
    }
    
    private String getListRedirect(User.userType userType) {
        switch (userType) {
            case ADMIN:
                return "redirect:/users/admin/customers";
            case STAFF:
                return "redirect:/users/staff/customers";
            default:
                return "redirect:/login";
        }
    }
}
