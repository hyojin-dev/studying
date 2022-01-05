package com.cdp.tdp.chat.domain;

import com.cdp.tdp.domain.Timestamped;
import com.cdp.tdp.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class ChatUser extends Timestamped {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne
    private User user;

    @JoinColumn(name = "ROOM_ID", nullable = false)
    @ManyToOne
    private ChatRoom chatRoom;

    public ChatUser(User user, ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        this.user = user;
    }

}