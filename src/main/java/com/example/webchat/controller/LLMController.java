package com.example.webchat.controller;

import com.example.webchat.DTO.ChatMessage;
import com.example.webchat.service.LLMService;
import com.example.webchat.service.MessageBrokerService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.Principal;

@RestController
public class LLMController {

    private final MessageBrokerService messageBrokerService;
    private final LLMService llmService;

    public LLMController(MessageBrokerService messageBrokerService, LLMService llmService) {
        this.messageBrokerService = messageBrokerService;
        this.llmService = llmService;
    }

    @MessageMapping("/chat/{chatId}/chat.sendToLLM")
    public void sendMessageToLLM(@DestinationVariable String chatId,
                                 ChatMessage message,
                                 Principal principal){
        //Сообщение от юзера, сохраняю и раскидываю как обычное сообщение.
        System.out.println("Сохраняю сообщение для LLM: " + message.getContent());
        String stringToLlmResponse = message.getContent();
        message.setContent("Запрос в LLM: " + message.getContent());
        messageBrokerService.sendMessage(chatId, message, principal);

        //Запрос в LLM, сохраняю и раскидываю ответ как обычное сообщение.
        try {
            String llmResponse = llmService.askLLM(stringToLlmResponse);
            ChatMessage aiMessage = new ChatMessage();
            aiMessage.setSender("AI");
            aiMessage.setChatId(chatId);
            aiMessage.setContent(llmResponse);
            aiMessage.setType("CHAT");
            messageBrokerService.sendMessage(chatId, aiMessage, principal);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
