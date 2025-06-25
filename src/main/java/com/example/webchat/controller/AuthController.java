package com.example.webchat.controller;

import com.example.webchat.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegistrationForm(@RequestParam String username,
                                          @RequestParam String password,
                                          @RequestParam String email){
        userService.registerNewUser(username, password, email);
        return "redirect:/login";
    }

    //При успешном логине редиректит на /profile в секьюр конфиге.
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        return "profile";
    }

}
