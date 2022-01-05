package com.cdp.tdp.chat.controller;

import com.cdp.tdp.chat.domain.ChatRoom;
import com.cdp.tdp.chat.domain.ChatUser;
import com.cdp.tdp.chat.dto.ChatMessageDTO;
import com.cdp.tdp.chat.repository.ChatRoomRepository;
import com.cdp.tdp.chat.repository.ChatUserRepository;
import com.cdp.tdp.domain.User;
import com.cdp.tdp.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.google.gson.Gson;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompChatController {

    private final SimpMessagingTemplate template; //특정 Broker로 메세지를 전달
    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserRepository chatUserRepository;
    private final UserRepository userRepository;

    //stompConfig에서 설정한 applicationDestinationPrefixes와 @MessageMapping 경로가 병합됨
    //"/pub/chat/enter"
    @MessageMapping(value = "/chat/enter")
    public void enter(ChatMessageDTO message) {
        String id = message.getRoomId();
        Long room_id = Long.valueOf(id);

        // chat user 정보 저장 (채팅유저 , 채팅방)
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(room_id).orElseThrow(
                () -> new NullPointerException("해당 채팅방이 존재하지 않습니다."));
        User user = userRepository.findByUsername(message.getWriter()).orElseThrow(
                () -> new NullPointerException("해당 사용자가 존재하지 않습니다."));


        if (!(chatUserRepository.findByChatRoomAndUser(chatRoom, user).isPresent())) { //채팅방 처음입장
            ChatUser chatUser = new ChatUser(user, chatRoom);
            chatUserRepository.save(chatUser);
            int count = chatUserRepository.countByChatRoom(chatRoom);

            chatRoom.setCount(count);
            chatRoomRepository.save(chatRoom);

            message.setMessage("채팅방에 참여하였습니다.");
            template.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
        } else {
            message.setMessage("채팅방에 재입장하였습니다.");
            template.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
        }
    }

    @Transactional
    @MessageMapping(value = "/chat/exit")
    public void exit(ChatMessageDTO message) {
        String id = message.getRoomId();
        Long room_id = Long.valueOf(id);

        // chat user 정보 삭제 (채팅유저 , 채팅방)
        ChatRoom chatRoom = chatRoomRepository.findById(room_id).orElseThrow(
                () -> new NullPointerException("해당 채팅방이 존재하지 않습니다."));
        User user = userRepository.findByUsername(message.getWriter()).orElseThrow(
                () -> new NullPointerException("해당 사용자가 존재하지 않습니다."));


        chatUserRepository.deleteByChatRoomAndUser(chatRoom, user);
        int count = chatUserRepository.countByChatRoom(chatRoom);
        chatRoom.setCount(count);
        chatRoomRepository.save(chatRoom);

        message.setMessage("채팅방을 나갔습니다.");
        template.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }

    @MessageMapping(value = "/chat/message")
    public void message(ChatMessageDTO message) {
        template.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }
}