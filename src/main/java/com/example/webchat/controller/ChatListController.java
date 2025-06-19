package com.example.webchat.controller;

import com.example.webchat.Entity.ChatEntity;
import com.example.webchat.model.UserEntity;
import com.example.webchat.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class ChatListController {

    private final UserRepository userRepository;

    public ChatListController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{username}/chats")
    public String getUserChats(@PathVariable String username, Model model) {
        Optional<UserEntity> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return "error"; // можно сделать кастомную страницу ошибки
        }

        List<ChatEntity> chats = user.get().getChats();
        model.addAttribute("chats", chats);
        model.addAttribute("username", username);
        return "user-chats";
    }
}
