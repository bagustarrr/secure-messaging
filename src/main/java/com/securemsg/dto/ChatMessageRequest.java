package com.securemsg.dto;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private String chatId;
    private String content;
}
