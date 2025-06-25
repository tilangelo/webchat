package com.example.webchat.controller;

import com.example.webchat.DTO.ChatMessage;
import com.example.webchat.Entity.ChatEntity;
import com.example.webchat.Entity.ChatMessageEntity;
import com.example.webchat.model.UserEntity;
import com.example.webchat.repository.ChatRepository;
import com.example.webchat.repository.MessagesRepository;
import com.example.webchat.repository.UserRepository;
import com.example.webchat.service.MessageBrokerService;
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

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessagesRepository messagesRepository;
    private final MessageBrokerService messageBrokerService;

    public ChatController(ChatRepository chatRepository, UserRepository userRepository,
                          MessagesRepository messagesRepository, MessageBrokerService messageBrokerService) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.messagesRepository = messagesRepository;
        this.messageBrokerService = messageBrokerService;
    }

    @Transactional
    @MessageMapping("/chat/{chatId}/chat.send")
    public void sendMessage(@DestinationVariable String chatId,
                            ChatMessage message,
                            Principal principal) {

        System.out.println("В контроллере отправки, имя принципала: "+ principal.getName());
        System.out.println("В контроллере отправки, чатайди: "+ chatId);
        System.out.println("В контроллере отправки, имя принципала: "+ principal);

        ChatEntity chat = chatRepository.findByChatId(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ваш не найден в бд"));
        if(!chatRepository.existsByChatIdAndUsername(chatId, principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "У вас нет прав на отправку сообщения");
        }

        message.setSender(principal.getName());
        messageBrokerService.saveMessage(message);
    }

    @GetMapping("/profile/chat_create")
    public String createChat(Principal principal, Model model) {
        ChatEntity chat = new ChatEntity();
        chat.generateChatId();
        UserEntity user = userRepository.findByUsername(principal.getName()).orElseThrow();
        chat.getUsers().add(user);
        user.getChats().add(chat);
        chatRepository.save(chat);
        userRepository.save(user);
        model.addAttribute("chatId", chat.getChatId());
        return "chat_room";
    }

    @GetMapping("/chat/{chatId}")
    public String chatRoom(@PathVariable String chatId, Model model, Principal principal) {

        if(principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        System.out.println("Принципал "+principal);

        System.out.println("Принципал Имя "+principal.getName());

        ChatEntity chat = chatRepository.findByChatId(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        System.out.println("Юзеры чата: " + chat.getUsers());
        if(!chat.getUsers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не можете просматривать этот чат");
        }
        model.addAttribute("messages", messagesRepository.findByChatId(chatId));
        model.addAttribute("chatId", chatId);
        return "chat_room";
    }

    @PostMapping("/chat/{chatId}/invite")
    public String invite(@PathVariable String chatId,
                         @RequestParam String usernameToInvite,
                         Principal principal) {
        ChatEntity chat = chatRepository.findByChatId(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ваш чат не найден в бд"));
        UserEntity currentUser = userRepository.findByUsername(principal.getName()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "вы являетесь не верным юзером чата")
        );
        if(!chat.getUsers().contains(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "У вас нет прав чтобы приглашать в этот чат");
        }
        UserEntity invitedUser = userRepository.findByUsername(usernameToInvite)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "нету пользователя с таким ником"));
        chat.getUsers().add(invitedUser);
        invitedUser.getChats().add(chat);
        userRepository.save(invitedUser);
        chatRepository.save(chat);
        return "redirect:/chat/" + chatId;
    }
}
