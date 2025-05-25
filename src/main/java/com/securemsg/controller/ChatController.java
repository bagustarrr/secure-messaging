package com.securemsg.controller;

import com.securemsg.model.ChatRoom;
import com.securemsg.model.Message;
import com.securemsg.model.User;
import com.securemsg.service.ChatService;
import com.securemsg.service.MessageService;
import com.securemsg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final MessageService messageService;
    private final UserRepository userRepository;

    @GetMapping("/chat")
    public String chatHome(@AuthenticationPrincipal User currentUser, Model model) {
        // Get all chats for the current user (sidebar list)
        List<ChatRoom> chats = chatService.getChatsForUser(currentUser.getIin());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("chats", chats);
        // No specific chat selected
        model.addAttribute("selectedChat", null);
        return "chat";
    }

    @GetMapping("/chat/{chatId}")
    public String openChat(@AuthenticationPrincipal User currentUser,
                           @PathVariable String chatId, Model model) {
        // Verify access to the chat
        ChatRoom chatRoom;
        try {
            chatRoom = chatService.getChatForUser(chatId, currentUser.getIin());
        } catch (IllegalArgumentException e) {
            // If chat not found or user not in chat, show error page (403/404)
            return "redirect:/error";
        }
        // Load chat history (messages)
        List<Message> messages = messageService.getMessagesForChat(chatId);
        // Prepare chat display name or title
        String chatTitle;
        if ("GROUP".equals(chatRoom.getType())) {
            chatTitle = chatRoom.getName();
        } else {
            // For private chat, find the other participant's name
            String otherIIN = chatRoom.getParticipants().stream()
                    .filter(iin -> !iin.equals(currentUser.getIin()))
                    .findFirst().orElse(currentUser.getIin());
            User otherUser = userRepository.findById(otherIIN).orElse(null);
            chatTitle = (otherUser != null ? otherUser.getFullName() : "Private Chat");
        }

        List<ChatRoom> chats = chatService.getChatsForUser(currentUser.getIin());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("chats", chats);
        model.addAttribute("selectedChat", chatRoom);
        model.addAttribute("chatTitle", chatTitle);
        model.addAttribute("messages", messages);
        return "chat";
    }

    @PostMapping("/chat/new")
    public String startNewChat(@AuthenticationPrincipal User currentUser,
                               @RequestParam("otherIIN") String otherIIN, Model model) {
        if (otherIIN == null || otherIIN.isBlank()) {
            return "redirect:/chat";
        }
        if (otherIIN.equals(currentUser.getIin())) {
            // Cannot start a chat with oneself (optional: could allow as personal notes)
            return "redirect:/chat?errorSelf";
        }
        // Verify target user exists
        if (!userRepository.existsById(otherIIN)) {
            return "redirect:/chat?errorUserNotFound";
        }
        // Create or find the private chat
        ChatRoom chatRoom = chatService.getOrCreatePrivateChat(currentUser.getIin(), otherIIN);
        return "redirect:/chat/" + chatRoom.getId();
    }
}
