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
import java.util.Optional;

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

    @PostMapping("/profile/chat_create")
    public String createChatPost(Principal principal, Model model, @RequestParam String chatName) {
        // Серверная валидация: минимум 5 латинских букв
        if (chatName == null || !chatName.matches("^[A-Za-z]{5,}$")) {
            model.addAttribute("username", principal.getName());
            model.addAttribute("chatCreateError", "Название чата должно содержать минимум 5 латинских букв без пробелов и других символов");
            return "profile";
        }
        ChatEntity chat = userService.createChat(principal, chatName);
        return "redirect:/profile/chat_create?redirect&chatId=" + chat.getChatId() + "&chatName=" + chat.getChatName();
    }

    @GetMapping(value = "/profile/chat_create", params = "redirect")
    public String createChatRedirect(@RequestParam String chatId, @RequestParam String chatName, Model model) {
        model.addAttribute("chatId", chatId);
        model.addAttribute("chatName", chatName);
        return "chat_room";
    }

    //Для рендера страницы чата по его chatId. Подгружает сообщения с помощью сервиса из БД. При наличии chatName у
    //чата, добавляет аттрибут для рендера chatName, чтобы отображалось имя чата.
    @GetMapping("/chat/{chatId}")
    public String chatRoom(@PathVariable String chatId, Model model, Principal principal) {
        List<ChatMessageEntity> messages = userService.renderChat(principal, chatId);
        Optional<ChatEntity> chatEntityOptional = userService.findChatById(chatId);

        model.addAttribute("messages", messages);
        model.addAttribute("chatId", chatId);

        if(chatEntityOptional.isPresent()) {
            ChatEntity chat = chatEntityOptional.get();
            model.addAttribute("chatName", chat.getChatName());
        }

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
