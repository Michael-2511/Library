package com.unibuc.library.service;

import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceInUseException;
import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.User;
import com.unibuc.library.model.UserProfile;
import com.unibuc.library.repository.LoanRepository;
import com.unibuc.library.repository.ReservationRepository;
import com.unibuc.library.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;

    public UserService(UserRepository userRepository,
                       LoanRepository loanRepository,
                       ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        this.reservationRepository = reservationRepository;
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

    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);

        userRepository.findByEmail(user.getEmail())
                .ifPresent(foundUser -> {
                    if (foundUser.getId() != id) {
                        throw new DuplicateResourceException(
                                "User with email '" + user.getEmail() + "' already exists"
                        );
                    }
                });

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setRole(user.getRole());
        existingUser.setMaxBorrowLimit(user.getMaxBorrowLimit());
        existingUser.setProfile(user.getProfile());

        UserProfile profile = existingUser.getProfile();
        if (profile != null) {
            profile.setUser(existingUser);
        }

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        User existingUser = getUserById(id);

        if (loanRepository.existsByUserId(id)) {
            throw new ResourceInUseException("User cannot be deleted because it has loan history");
        }
        if (reservationRepository.existsByUserId(id)) {
            throw new ResourceInUseException("User cannot be deleted because it has reservations");
        }

        userRepository.delete(existingUser);
    }
}
