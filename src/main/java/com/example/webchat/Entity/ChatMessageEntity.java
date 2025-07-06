package com.example.webchat.Entity;

import com.example.webchat.DTO.ChatMessage;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ChatMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String sender;
    @Column(length = 10000)
    private String content;
    private String type; // "CHAT", "JOIN", "LEAVE"
    @Column(name = "chat_id")
    private String chatId;

    private LocalDateTime timestamp;

    public ChatMessageEntity(ChatMessage message) {
        this.sender = message.getSender();
        this.content = message.getContent();
        this.type = message.getType();
        this.chatId = message.getChatId();
    }

    public ChatMessageEntity() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
