package com.example.webchat.repository;

import com.example.webchat.Entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
    Optional<ChatEntity> findByChatId(String chatId);
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ChatEntity c JOIN c.users u WHERE c.chatId = :chatId AND u.username = :username")
    boolean existsByChatIdAndUsername(@Param("chatId") String chatId, @Param("username") String username);
}
