package com.springboot.project.controller;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import com.springboot.project.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.springboot.project.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import jakarta.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AuthController {
    @Autowired
    private UserService userService;
    
    // Login page
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    // Register page
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Process login
    @PostMapping("/login")
    public String processLogin(@RequestParam String username, 
                             @RequestParam String password,
                             HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (userService.validateUser(username, password)) {
            User user = userService.findByUsername(username).get();
            session.setAttribute("user", user);
            session.setAttribute("userType", user.getType());

            // Redirect based on user type
            switch (user.getType()) {
                case ADMIN:
                    return "redirect:/admin/dashboard";
                case STAFF:
                    return "redirect:/staff/dashboard";
                case CUSTOMER:
                default:
                    return "redirect:/customer/dashboard";
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/login";
        }
    }
    
    // Process registration
    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute User user,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        
        if (bindingResult.hasErrors()) {
            return "register";
        }
        
        try {
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
    
    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // Dashboard controllers
    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getType() != User.userType.ADMIN) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "admin-dashboard";
    }
    
    @GetMapping("/staff/dashboard")
    public String staffDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getType() != User.userType.STAFF) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "staff-dashboard";
    }
    
    @GetMapping("/customer/dashboard")
    public String customerDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getType() != User.userType.CUSTOMER) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "customer-dashboard";
    }
    
    // Home page redirect
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
}
