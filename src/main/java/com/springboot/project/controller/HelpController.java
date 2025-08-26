package com.springboot.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import com.springboot.project.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

@Controller
public class HelpController {

    @GetMapping("/help")
    public String help(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "help";
    }
}
