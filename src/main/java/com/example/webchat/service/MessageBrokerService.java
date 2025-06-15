package com.example.webchat.service;

import com.example.webchat.DTO.ChatMessage;
import com.example.webchat.Entity.ChatMessageEntity;
import com.example.webchat.repository.MessagesRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MessageBrokerService {
    private final MessagesRepository messagesRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageBrokerService(MessagesRepository messagesRepository, SimpMessagingTemplate messagingTemplate) {
        this.messagesRepository = messagesRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void saveMessage(ChatMessage message) {
        message.setChatId(message.getChatId().replaceAll("^\"|\"$", ""));
        ChatMessageEntity entity = new ChatMessageEntity(message);
        entity.setTimestamp(LocalDateTime.now());
        messagesRepository.save(entity);
        System.out.println("New ID: " + entity.getChatId());
        convertAndSend(message);
    }

    public void convertAndSend(ChatMessage message) {
        message.setChatId(message.getChatId().replaceAll("^\"|\"$", ""));
        messagingTemplate.convertAndSend("/topic/chat/" + message.getChatId(), message);
    }
}
