package com.cdp.tdp.chat.service;

import com.cdp.tdp.chat.domain.ChatRoom;
import com.cdp.tdp.chat.dto.ChatRoomDTO;
import com.cdp.tdp.chat.repository.ChatRoomRepository;
import com.cdp.tdp.domain.User;
import com.cdp.tdp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public List<ChatRoom> getAllRooms() {
        return chatRoomRepository.findAll();
    }

    public ChatRoom createRoom(ChatRoomDTO chatRoomDTO, Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("no such user"));
        ChatRoom room = new ChatRoom(chatRoomDTO, user);
        chatRoomRepository.save(room);
        return room;
    }

    public ChatRoom getRoom(Long roomId) {
        return chatRoomRepository.findById(roomId).orElseThrow(
                () -> new NullPointerException("해당 채팅방이 존재하지 않습니다")
        );
    }

    public boolean checkMyRoom(Long roomId, Long nowUserId) {
        ChatRoom chatRoom = getRoom(roomId);
        if (chatRoom.getUser().getId().equals(nowUserId)) {
            return true;
        }
        return false;
    }

    @Transactional
    public String deleteRoom(Long id, Long nowUserId) {
        if (!id.equals(nowUserId)) {
            throw new AccessDeniedException("채팅방을 삭제할 권한이 없습니다.");
        }
        chatRoomRepository.deleteById(id);
        return "채팅방 삭제가 완료되었습니다.";
    }
}
