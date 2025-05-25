package com.securemsg.repository;

import com.securemsg.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByIin(String iin);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}