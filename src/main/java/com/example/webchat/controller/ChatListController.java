package com.example.webchat.controller;

import com.example.webchat.Entity.ChatEntity;
import com.example.webchat.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class ChatListController {

    private final UserService userService;

    public ChatListController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}/chats")
    public String getUserChats(@PathVariable String username, Model model, Principal principal) {
        List<ChatEntity> chats = userService.renderChats(principal, username);
        model.addAttribute("chats", chats);
        model.addAttribute("username", username);
        return "user-chats";
    }
}
