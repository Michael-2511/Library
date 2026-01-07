package com.unibuc.library.service;

import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.User;
import com.unibuc.library.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {

        userRepository.findByEmail(user.getEmail())
                .ifPresent(existingUser -> {
                    throw new DuplicateResourceException(
                            "User with email '" + user.getEmail() + "' already exists"
                    );
                });

        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id
                ));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
