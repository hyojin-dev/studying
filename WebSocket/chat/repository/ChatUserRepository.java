package com.cdp.tdp.chat.repository;

import com.cdp.tdp.chat.domain.ChatRoom;
import com.cdp.tdp.chat.domain.ChatUser;
import com.cdp.tdp.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {

    Optional<ChatUser> deleteByChatRoomAndUser(ChatRoom chatRoom, User user);
    Optional<ChatUser> findByChatRoomAndUser(ChatRoom chatRoom, User user);

    int countByChatRoom(ChatRoom chatRoom);
}
