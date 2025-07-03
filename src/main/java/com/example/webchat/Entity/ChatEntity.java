package com.example.webchat.Entity;

import com.example.webchat.model.UserEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class ChatEntity {
    @Column(unique = true)
    private String ChatName;

    @Column(unique = true, nullable = false)
    private String chatId;

    @ManyToMany(mappedBy = "chats")
    private List<UserEntity> users = new ArrayList<>();

    @PrePersist
    public void generateChatId(){
        this.chatId = UUID.randomUUID().toString();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    public String getChatName() {
        return ChatName;
    }

    public void setChatName(String chatName) {
        ChatName = chatName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public List<UserEntity> getUsers() {
        return users;
    }
}
