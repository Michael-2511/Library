package com.unibuc.library.service;

import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.User;
import com.unibuc.library.model.UserRole;
import com.unibuc.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setRole(UserRole.MEMBER);
        user.setMaxBorrowLimit(5);
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = userService.createUser(user);

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getRole(), result.getRole());
        assertEquals(user.getMaxBorrowLimit(), result.getMaxBorrowLimit());
        verify(userRepository).findByEmail(user.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void createUser_DuplicateEmail_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act & Assert
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> userService.createUser(user));

        assertEquals("User with email 'john.doe@example.com' already exists", exception.getMessage());
        verify(userRepository).findByEmail(user.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals(UserRole.MEMBER, result.getRole());
        assertEquals(5, result.getMaxBorrowLimit());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(999L));

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository).findById(999L);
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setName("Jane Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setRole(UserRole.LIBRARIAN);
        user2.setMaxBorrowLimit(10);

        List<User> users = Arrays.asList(user, user2);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals(UserRole.MEMBER, result.get(0).getRole());
        assertEquals("Jane Smith", result.get(1).getName());
        assertEquals(UserRole.LIBRARIAN, result.get(1).getRole());
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void createUser_WithDifferentEmail_Success() {
        // Arrange
        User newUser = new User();
        newUser.setId(2L);
        newUser.setName("Jane Smith");
        newUser.setEmail("jane.smith@example.com");
        newUser.setRole(UserRole.LIBRARIAN);
        newUser.setMaxBorrowLimit(10);

        when(userRepository.findByEmail(newUser.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        User result = userService.createUser(newUser);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("jane.smith@example.com", result.getEmail());
        assertEquals(UserRole.LIBRARIAN, result.getRole());
        assertEquals(10, result.getMaxBorrowLimit());
        verify(userRepository).findByEmail(newUser.getEmail());
        verify(userRepository).save(newUser);
    }

    @Test
    void createUser_Librarian_Success() {
        // Arrange
        User librarian = new User();
        librarian.setId(3L);
        librarian.setName("Alice Admin");
        librarian.setEmail("alice.admin@example.com");
        librarian.setRole(UserRole.LIBRARIAN);
        librarian.setMaxBorrowLimit(20);

        when(userRepository.findByEmail(librarian.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(librarian);

        // Act
        User result = userService.createUser(librarian);

        // Assert
        assertNotNull(result);
        assertEquals(UserRole.LIBRARIAN, result.getRole());
        assertEquals(20, result.getMaxBorrowLimit());
        verify(userRepository).findByEmail(librarian.getEmail());
        verify(userRepository).save(librarian);
    }

    @Test
    void createUser_DifferentBorrowLimits_Success() {
        // Arrange
        User memberUser = new User();
        memberUser.setId(4L);
        memberUser.setName("Bob Reader");
        memberUser.setEmail("bob.reader@example.com");
        memberUser.setRole(UserRole.MEMBER);
        memberUser.setMaxBorrowLimit(3);

        when(userRepository.findByEmail(memberUser.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(memberUser);

        // Act
        User result = userService.createUser(memberUser);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getMaxBorrowLimit());
        verify(userRepository).save(memberUser);
    }
}