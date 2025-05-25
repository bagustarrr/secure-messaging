package com.securemsg.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document("chatrooms")
public class ChatRoom {
    @Id
    private String id;
    private String name;
    private String type; // "GROUP" or "PRIVATE"
    private List<String> participants;
    private String createdBy;

    public ChatRoom() {
        this.id = new ObjectId().toHexString();
    }
}
