package com.securemsg.controller;


import com.securemsg.model.User;
import com.securemsg.repository.UserRepository;
import com.securemsg.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ProfileController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal User currentUser, Model model) {
        // Reload user from database to get fresh data
        User user = userRepository.findById(currentUser.getIin()).orElse(currentUser);
        model.addAttribute("currentUser", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal User currentUser,
                                @RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam String gender,
                                @RequestParam(required = false) String birthDate,
                                @RequestParam(required = false) String newPassword,
                                @RequestParam(required = false) String confirmPassword,
                                @RequestParam(required = false) String theme,
                                @RequestParam(required = false) String language,
                                @RequestParam("photoFile") MultipartFile photoFile,
                                Model model) {
        // Fetch the up-to-date user from DB
        User user = userRepository.findById(currentUser.getIin()).orElseThrow();
        String originalEmail = user.getEmail();

        // Validate and apply changes:
        if (!email.equalsIgnoreCase(originalEmail)) {
            if (userRepository.findByEmail(email).isPresent()) {
                model.addAttribute("error", "Email is already taken by another user.");
                model.addAttribute("currentUser", user);  // include current data for re-display
                return "profile";
            }
            user.setEmail(email);
        }
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setGender(gender);
        if (birthDate != null && !birthDate.isEmpty()) {
            try {
                user.setBirthDate(LocalDate.parse(birthDate));
            } catch (Exception e) {
                // If date parse fails, ignore or handle accordingly
            }
        }
        if (theme != null && !theme.isEmpty()) {
            user.setTheme(theme);
        }
        if (language != null && !language.isEmpty()) {
            user.setLanguage(language);
        }
        if (newPassword != null && !newPassword.isBlank()) {
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("error", "New passwords do not match.");
                model.addAttribute("currentUser", user);
                return "profile";
            }
            if (newPassword.length() < 6) {
                model.addAttribute("error", "New password must be at least 6 characters.");
                model.addAttribute("currentUser", user);
                return "profile";
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                String contentType = photoFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    model.addAttribute("error", "Profile photo must be an image file.");
                    model.addAttribute("currentUser", user);
                    return "profile";
                }
                // Convert image to Base64 data URI
                byte[] bytes = photoFile.getBytes();
                String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
                String ext = contentType.substring(6); // e.g., "jpeg" or "png"
                String dataUri = "data:" + contentType + ";base64," + base64;
                user.setPhoto(dataUri);
            } catch (Exception e) {
                model.addAttribute("error", "Failed to upload photo.");
                model.addAttribute("currentUser", user);
                return "profile";
            }
        }
        // Save updates
        userRepository.save(user);
        return "redirect:/profile?success";
    }
}