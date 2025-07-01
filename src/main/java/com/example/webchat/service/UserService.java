package com.example.webchat.service;

import com.example.webchat.Entity.ChatEntity;
import com.example.webchat.Entity.ChatMessageEntity;
import com.example.webchat.model.UserEntity;
import com.example.webchat.repository.ChatRepository;
import com.example.webchat.repository.MessagesRepository;
import com.example.webchat.repository.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChatRepository chatRepository;
    private final MessagesRepository messagesRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ChatRepository chatRepository, MessagesRepository messagesRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.chatRepository = chatRepository;
        this.messagesRepository = messagesRepository;
    }

    /**
     * Добавляет нового пользователя в БД(users). Ставит из параметров имя, пароль(в виде хеша bcrypt), mail,
     * @param username
     * @param password
     * @param email
     * @return
     */
    public UserEntity registerNewUser(String username, String password, String email) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(passwordEncoder.encode(password));
        userEntity.setEmail(email);
        return userRepository.save(userEntity);
    }

    /**
     * Создаёт новый чат. Добавляет чату пользователя, а пользователю чат.
     * @param principal принципал
     * @return Возвращает ChatEntity
     */
    public ChatEntity createChat(Principal principal){
        ChatEntity chat = new ChatEntity();
        chat.generateChatId();
        UserEntity user = userRepository.findByUsername(principal.getName()).orElseThrow();
        chat.getUsers().add(user);
        user.getChats().add(chat);
        chatRepository.save(chat);
        userRepository.save(user);
        return chat;
    }

    /**
     * Используется для подгрузки сообщений определённого чата из БД. Валидация авторизации, айди чата,
     * имени авторизованного юзера(Имеет ли доступ)
     * @param principal принципал
     * @param chatId айди чата
     * @return Список сообщений чата
     */
    public List<ChatMessageEntity> renderChat(Principal principal, String chatId){
        if(principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        ChatEntity chat = chatRepository.findByChatId(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if(!chat.getUsers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не можете просматривать этот чат");
        }
        return messagesRepository.findByChatId(chatId);
    }

    /**
     * Используется для возвращения списка чатов юзера. Проверка авторизации, валидация имени авторизованного.
     * @param principal принципал
     * @param username имя юзера из строки поиска (/username/chat_list)
     * @return Список чатов ChatEntity
     */
    public List<ChatEntity> renderChats(Principal principal, String username){

        if(principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Для начала выполните вход в аккаунт");
        }

        if(!principal.getName().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Отказано в доступе - это не ваш список");
        }

        Optional<UserEntity> user = userRepository.findByUsername(principal.getName());
        if (user.isEmpty()) {
            //TODO
        }

        System.out.println("Список чатов: " + user.get().getChats());

        return user.get().getChats();
    }

    /**Приглашение нового юзера в чат: валидация входного чатАйди, имени Принципала, наличия имени пользователя
     * в списке юзеров чата, наличия в БД юзера которого приглашают. Добавляет чату пользователя, а пользователю чат.
     *
     * @param principal принципал.
     * @param chatId АйдиЧата.
     * @param usernameToInvite имяЮзера для приглашения.
     */
    public void inviteUser(Principal principal, String chatId, String usernameToInvite) {
        ChatEntity chat = chatRepository.findByChatId(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ваш чат не найден в бд"));
        UserEntity currentUser = userRepository.findByUsername(principal.getName()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "вы являетесь неверным юзером чата")
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
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return new User(user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
