package com.securemsg.service;

import com.securemsg.model.ChatRoom;
import com.securemsg.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;

    public List<ChatRoom> getChatsForUser(String userIin) {
        return chatRoomRepository.findByParticipantsContaining(userIin);
    }

    public ChatRoom getOrCreatePrivateChat(String userIin1, String userIin2) {
        List<ChatRoom> possibleChats = chatRoomRepository.findByParticipantsContaining(userIin1);
        for (ChatRoom chat : possibleChats) {
            if ("PRIVATE".equals(chat.getType()) && chat.getParticipants().contains(userIin2) && chat.getParticipants().size() == 2) {
                return chat;
            }
        }
        ChatRoom newChat = new ChatRoom();
        newChat.setType("PRIVATE");
        newChat.setName(null);
        List<String> participants = new ArrayList<>();
        participants.add(userIin1);
        participants.add(userIin2);
        newChat.setParticipants(participants);
        newChat.setCreatedBy(userIin1);
        return chatRoomRepository.save(newChat);
    }

    public ChatRoom createGroupChat(String name, List<String> participantIins, String creatorIin) {
        List<String> participants = new ArrayList<>(participantIins.stream().distinct().toList());
        if (!participants.contains(creatorIin)) {
            participants.add(creatorIin);
        }
        ChatRoom group = new ChatRoom();
        group.setType("GROUP");
        group.setName(name);
        group.setParticipants(participants);
        group.setCreatedBy(creatorIin);
        return chatRoomRepository.save(group);
    }

    public ChatRoom getChatForUser(String chatId, String userIin) {
        ChatRoom chat = chatRoomRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));
        if (!chat.getParticipants().contains(userIin)) {
            throw new IllegalArgumentException("Access denied to this chat");
        }
        return chat;
    }

    public boolean isUserInChat(String chatId, String userIin) {
        Optional<ChatRoom> opt = chatRoomRepository.findById(chatId);
        return opt.isPresent() && opt.get().getParticipants().contains(userIin);
    }

    public void deleteChat(String chatId) {
        chatRoomRepository.findById(chatId).ifPresent(chat -> {
            if ("GROUP".equals(chat.getType())) {
                chatRoomRepository.deleteById(chatId);
            }
        });
    }

    public List<ChatRoom> getAllGroupChats() {
        return chatRoomRepository.findByType("GROUP");
    }
}