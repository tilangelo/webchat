package com.example.webchat.controller;

import com.example.webchat.DTO.ChatMessage;
import com.example.webchat.Entity.ChatEntity;
import com.example.webchat.Entity.ChatMessageEntity;
import com.example.webchat.model.UserEntity;
import com.example.webchat.repository.ChatRepository;
import com.example.webchat.repository.MessagesRepository;
import com.example.webchat.repository.UserRepository;
import com.example.webchat.service.MessageBrokerService;
import com.example.webchat.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@Controller
public class ChatController {

    private final MessageBrokerService messageBrokerService;
    private final UserService userService;

    public ChatController(MessageBrokerService messageBrokerService, UserService userService) {
        this.messageBrokerService = messageBrokerService;
        this.userService = userService;
    }

    @MessageMapping("/chat/{chatId}/chat.send")
    public void sendMessage(@DestinationVariable String chatId,
                            ChatMessage message,
                            Principal principal) {
        messageBrokerService.sendMessage(chatId, message, principal);
    }

    @GetMapping("/profile/chat_create")
    public String createChat(Principal principal, Model model) {
        ChatEntity chat = userService.createChat(principal);
        model.addAttribute("chatId", chat.getChatId());
        return "chat_room";
    }

    @GetMapping("/chat/{chatId}")
    public String chatRoom(@PathVariable String chatId, Model model, Principal principal) {
        List<ChatMessageEntity> messages = userService.renderChat(principal, chatId);
        model.addAttribute("messages", messages);
        model.addAttribute("chatId", chatId);
        return "chat_room";
    }

    @PostMapping("/chat/{chatId}/invite")
    public String invite(@PathVariable String chatId,
                         @RequestParam String usernameToInvite,
                         Principal principal) {
        userService.inviteUser(principal, chatId, usernameToInvite);
        return "redirect:/chat/" + chatId;
    }
}
