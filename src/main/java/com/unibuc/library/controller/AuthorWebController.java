package com.unibuc.library.controller;

import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceInUseException;
import com.unibuc.library.model.Author;
import com.unibuc.library.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/authors")
public class AuthorWebController {

    private final AuthorService authorService;

    public AuthorWebController(AuthorService authorService) {
        this.authorService = authorService;
    }

    // ── LIST ──────────────────────────────────────────────────────────────

    @GetMapping
    public String listAuthors(@RequestParam(required = false) String name, Model model) {
        List<Author> authors;
        boolean searching = name != null && !name.isBlank();

        if (searching) {
            authors = authorService.getAllAuthors().stream()
                    .filter(a -> a.getName().toLowerCase().contains(name.toLowerCase()))
                    .toList();
        } else {
            authors = authorService.getAllAuthors();
        }

        model.addAttribute("authors", authors);
        model.addAttribute("searchName", name);
        model.addAttribute("searching", searching);
        return "authors/list";
    }

    // ── DETAIL ────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public String viewAuthor(@PathVariable Long id, Model model) {
        model.addAttribute("author", authorService.getAuthorById(id));
        return "authors/detail";
    }

    // ── CREATE ────────────────────────────────────────────────────────────

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("author", new Author());
        model.addAttribute("pageTitle", "Add New Author");
        return "authors/form";
    }

    @PostMapping("/new")
    public String createAuthor(@Valid @ModelAttribute("author") Author author,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Add New Author");
            return "authors/form";
        }
        try {
            authorService.createAuthor(author);
            redirectAttributes.addFlashAttribute("successMessage", "Author created successfully!");
            return "redirect:/authors";
        } catch (DuplicateResourceException e) {
            result.rejectValue("name", "name.duplicate", e.getMessage());
            model.addAttribute("pageTitle", "Add New Author");
            return "authors/form";
        }
    }

    // ── EDIT ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("author", authorService.getAuthorById(id));
        model.addAttribute("pageTitle", "Edit Author");
        return "authors/form";
    }

    @PostMapping("/{id}/edit")
    public String updateAuthor(@PathVariable Long id,
                               @Valid @ModelAttribute("author") Author author,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Author");
            return "authors/form";
        }
        try {
            authorService.updateAuthor(id, author);
            redirectAttributes.addFlashAttribute("successMessage", "Author updated successfully!");
            return "redirect:/authors";
        } catch (DuplicateResourceException e) {
            result.rejectValue("name", "name.duplicate", e.getMessage());
            model.addAttribute("pageTitle", "Edit Author");
            return "authors/form";
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    public String deleteAuthor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            authorService.deleteAuthor(id);
            redirectAttributes.addFlashAttribute("successMessage", "Author deleted successfully.");
        } catch (ResourceInUseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/authors";
    }
}
