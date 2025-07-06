package com.example.webchat.service;

import com.example.webchat.DTO.ChatMessage;
import com.example.webchat.Entity.ChatEntity;
import com.example.webchat.Entity.ChatMessageEntity;
import com.example.webchat.repository.ChatRepository;
import com.example.webchat.repository.MessagesRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;

@Service
public class MessageBrokerService {
    private final MessagesRepository messagesRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRepository chatRepository;

    public MessageBrokerService(MessagesRepository messagesRepository, SimpMessagingTemplate messagingTemplate, ChatRepository chatRepository) {
        this.messagesRepository = messagesRepository;
        this.messagingTemplate = messagingTemplate;
        this.chatRepository = chatRepository;
    }

    /**
     * Валидирует чат по его id, авторизованного пользователя на нахождение онного в списке юзеров чата.
     * Для сообщения сохраняет отправителя. Далее использует метод saveMessage для сохранения в БД и отправки в WebSocket.
     * @param chatId айдиЧата
     * @param message Сообщение из чата
     * @param principal принципал.
     */
    @Transactional
    public void sendMessage(String chatId, ChatMessage message, Principal principal) {

        System.out.println("В контроллере отправки, имя принципала: "+ principal.getName());
        System.out.println("В контроллере отправки, чатайди: "+ chatId);
        System.out.println("В контроллере отправки, имя принципала: "+ principal);

        ChatEntity chat = chatRepository.findByChatId(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ваш чат не найден в бд"));
        if(!chatRepository.existsByChatIdAndUsername(chatId, principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "У вас нет прав на отправку сообщения");
        }
        if(message.getSender() == null || !message.getSender().equals("AI")) {
            message.setSender(principal.getName());
        }
        saveMessage(message);
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
