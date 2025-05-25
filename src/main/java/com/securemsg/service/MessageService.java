package com.securemsg.service;

import com.securemsg.model.Message;
import com.securemsg.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;

    public Message saveMessage(String chatId, String senderIin, String content) {
        Message msg = new Message();
        msg.setChatId(chatId);
        msg.setSender(senderIin);
        msg.setContent(content);
        msg.setTimestamp(new Date());
        return messageRepository.save(msg);
    }

    public List<Message> getMessagesForChat(String chatId) {
        return messageRepository.findByChatIdOrderByTimestampAsc(chatId);
    }

    public void deleteMessagesByChat(String chatId) {
        messageRepository.deleteByChatId(chatId);
    }
}