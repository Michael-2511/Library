package com.unibuc.library.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Handles exceptions for MVC (Thymeleaf) views.
 * Only intercepts requests that are NOT under /rest/** so that REST controllers
 * keep their own JSON error responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /** Returns true when the request is a browser/MVC request (not a REST call). */
    private boolean isMvcRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.startsWith("/rest/");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex,
                                 HttpServletRequest request,
                                 Model model) {
        if (!isMvcRequest(request)) return null; // let REST handle it
        model.addAttribute("errorTitle", "Resource Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("statusCode", 404);
        return "error/404";
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicate(DuplicateResourceException ex,
                                  HttpServletRequest request,
                                  Model model) {
        if (!isMvcRequest(request)) return null;
        model.addAttribute("errorTitle", "Duplicate Resource");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("statusCode", 409);
        return "error/error";
    }

    @ExceptionHandler(ResourceInUseException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleInUse(ResourceInUseException ex,
                              HttpServletRequest request,
                              Model model) {
        if (!isMvcRequest(request)) return null;
        model.addAttribute("errorTitle", "Resource In Use");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("statusCode", 409);
        return "error/error";
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoHandler(Exception ex,
                                  HttpServletRequest request,
                                  Model model) {
        if (!isMvcRequest(request)) return null;
        model.addAttribute("errorTitle", "Page Not Found");
        model.addAttribute("errorMessage", "The page you are looking for does not exist.");
        model.addAttribute("statusCode", 404);
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception ex,
                                HttpServletRequest request,
                                Model model) {
        if (!isMvcRequest(request)) return null;
        model.addAttribute("errorTitle", "Internal Server Error");
        model.addAttribute("errorMessage", "Something went wrong on our end. Please try again later.");
        model.addAttribute("statusCode", 500);
        return "error/500";
    }
}
