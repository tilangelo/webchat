package com.example.webchat.controller;

import com.example.webchat.DTO.ChatMessage;
import com.example.webchat.Entity.ChatEntity;
import com.example.webchat.Entity.ChatMessageEntity;
import com.example.webchat.repository.ChatRepository;
import com.example.webchat.repository.MessagesRepository;
import com.example.webchat.service.MessageBrokerService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Controller
public class ChatController {

    private final ChatRepository chatRepository;
    private final MessagesRepository messagesRepository;
    private final MessageBrokerService messageBrokerService;

    public ChatController(ChatRepository chatRepository, MessagesRepository messagesRepository, MessageBrokerService messageBrokerService) {
        this.chatRepository = chatRepository;
        this.messagesRepository = messagesRepository;
        this.messageBrokerService = messageBrokerService;
    }

    @MessageMapping("/chat.send")
    public ChatMessage sendMessage(ChatMessage message) {
        System.out.println("Old ID");
        System.out.println(message.getChatId());
        messageBrokerService.saveMessage(message);
        return message;
    }

    @GetMapping("/chat_list")
    public String getChatListPage(Model model) {
        List<ChatEntity> chats = chatRepository.findAll();
        model.addAttribute("chats", chats);
        return "chat_list";
    }

    @GetMapping("/chat_create")
    public String createChat() {
        ChatEntity chat = new ChatEntity();
        chat.generateChatId();
        chatRepository.save(chat);
        return "SYKA";
    }

    @GetMapping("/chat/{chatId}")
    public String chatRoom(@PathVariable String chatId, Model model) {
        List<ChatMessageEntity> messages = messagesRepository.findByChatId(chatId);
        model.addAttribute("messages", messages);
        model.addAttribute("chatId", chatId);
        return "chat_room";
    }
}
