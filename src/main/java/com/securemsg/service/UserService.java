package com.securemsg.service;

import com.securemsg.model.User;
import com.securemsg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        if (userRepository.findById(user.getIin()).isPresent()) {
            throw new IllegalArgumentException("A user with this IIN already exists.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already in use by another account.");
        }
        String role = user.getRole() != null ? user.getRole().toUpperCase() : "STUDENT";
        if (!role.equals("STUDENT") && !role.equals("TEACHER")) {
            throw new IllegalArgumentException("Invalid role selection.");
        }
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getTheme() == null) user.setTheme("dark");
        if (user.getLanguage() == null) user.setLanguage("en");
        return userRepository.save(user);
    }

    public Optional<User> getByIIN(String iin) {
        return userRepository.findById(iin);
    }

    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }
}
