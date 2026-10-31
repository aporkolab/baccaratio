package com.prokey.baccaratio.service;

import com.prokey.baccaratio.model.User;
import com.prokey.baccaratio.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    static final String USERNAME_TAKEN = "Username is already taken!";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user with a hashed password.
     *
     * @throws IllegalArgumentException if the username is already taken
     */
    @Transactional
    public User register(String username, String rawPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException(USERNAME_TAKEN);
        }
        try {
            // saveAndFlush surfaces the unique constraint violation of a concurrent registration here
            return userRepository.saveAndFlush(new User(username, passwordEncoder.encode(rawPassword)));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(USERNAME_TAKEN, e);
        }
    }
}
