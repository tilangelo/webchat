package com.example.webchat.repository;

import com.example.webchat.DTO.ChatMessage;
import com.example.webchat.Entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessagesRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findByChatId(String chatId);
}
