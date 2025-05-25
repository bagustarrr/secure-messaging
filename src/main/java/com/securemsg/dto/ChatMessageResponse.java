package com.securemsg.dto;

import lombok.Data;

@Data
public class ChatMessageResponse {
    private String chatId;
    private String sender;
    private String senderName;
    private String content;
    private String timestamp;
}