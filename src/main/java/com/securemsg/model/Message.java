package com.securemsg.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document("messages")
public class Message {
    @Id
    private String id;
    @Indexed
    private String chatId;
    private String sender;
    private String content;
    private Date timestamp;
}
