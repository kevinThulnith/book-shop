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
}
