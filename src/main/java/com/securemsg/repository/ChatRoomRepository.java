package com.securemsg.repository;

import com.securemsg.model.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    List<ChatRoom> findByParticipantsContaining(String iin);
    List<ChatRoom> findByType(String type);
}