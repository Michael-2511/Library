package com.unibuc.library.controller;

import com.unibuc.library.model.User;
import com.unibuc.library.model.UserRole;
import com.unibuc.library.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SignupController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(@ModelAttribute("user") User user,
                               BindingResult result,
                               Model model) {
        // Validate required fields manually
        if (user.getName() == null || user.getName().isBlank()) {
            result.rejectValue("name", "name.blank", "Name is required");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            result.rejectValue("email", "email.blank", "Email is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            result.rejectValue("password", "password.blank", "Password is required");
        }

        // Check if email already exists
        if (user.getEmail() != null && !user.getEmail().isBlank()
                && userRepository.findByEmail(user.getEmail()).isPresent()) {
            result.rejectValue("email", "email.duplicate", "An account with this email already exists");
        }

        if (result.hasErrors()) {
            return "signup";
        }

        // Set member defaults
        user.setRole(UserRole.MEMBER);
        user.setMaxBorrowLimit(5);
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
        return "redirect:/login?registered";
    }
}
