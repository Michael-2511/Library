package com.unibuc.library.controller;

import com.unibuc.library.model.Loan;
import com.unibuc.library.model.User;
import com.unibuc.library.model.UserRole;
import com.unibuc.library.repository.BookRepository;
import com.unibuc.library.repository.UserRepository;
import com.unibuc.library.service.LoanService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/loans")
public class LoanWebController {

    private final LoanService loanService;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public LoanWebController(LoanService loanService,
                             UserRepository userRepository,
                             BookRepository bookRepository) {
        this.loanService = loanService;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public String listLoans(@RequestParam(defaultValue = "active") String filter,
                            Model model,
                            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        boolean isMember = currentUser.getRole() == UserRole.MEMBER;

        List<Loan> loans;
        if (isMember) {
            // Members only see their own loans
            loans = switch (filter) {
                case "overdue" -> loanService.getOverdueLoans().stream()
                        .filter(loan -> loan.getUser().getId() == currentUser.getId())
                        .toList();
                case "all" -> loanService.getLoanHistoryByUser(currentUser.getId());
                default -> loanService.getActiveLoansByUser(currentUser.getId());
            };
        } else {
            // Librarians and Admins see all loans
            loans = switch (filter) {
                case "overdue" -> loanService.getOverdueLoans();
                case "all" -> loanService.getAllLoans();
                default -> loanService.getActiveLoans();
            };
        }

        model.addAttribute("loans", loans);
        model.addAttribute("filter", filter);
        model.addAttribute("isMember", isMember);
        return "loans/list";
    }

    @GetMapping("/new")
    public String showBorrowForm(Model model, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        boolean isMember = currentUser.getRole() == UserRole.MEMBER;

        if (isMember) {
            // Members can only borrow for themselves
            model.addAttribute("currentUser", currentUser);
        } else {
            // Librarians/Admins can select any user
            model.addAttribute("users", userRepository.findAll());
        }
        model.addAttribute("isMember", isMember);
        model.addAttribute("books", bookRepository.findAllWithAuthorsAndCategory());
        return "loans/form";
    }

    @PostMapping("/new")
    public String borrowBook(@RequestParam(required = false) Long userId,
                             @RequestParam Long bookId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser(authentication);
            // Members always borrow for themselves
            if (currentUser.getRole() == UserRole.MEMBER) {
                userId = currentUser.getId();
            }
            loanService.borrowBook(userId, bookId);
            redirectAttributes.addFlashAttribute("successMessage", "Book borrowed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/loans";
    }

    @PostMapping("/{id}/return")
    public String returnBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            loanService.returnBook(id);
            redirectAttributes.addFlashAttribute("successMessage", "Book returned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/loans";
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));
    }
}
