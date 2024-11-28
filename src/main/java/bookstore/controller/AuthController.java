package bookstore.controller;

import bookstore.dto.UserDto;
import bookstore.models.User;
import bookstore.service.UserService;
import bookstore.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("requestUri")
    public String getRequestServletPath(final HttpServletRequest request) {
        return request.getRequestURI();
    }

    // Register Page
    @GetMapping("/register")
    public String showRegistrationForm(Model model, HttpServletRequest request, HttpServletResponse response) {
        if (SecurityUtil.isAuthenticated()) {
            return SecurityUtil.handleAuthenticatedUserRedirect(request, response, "/auth/dashboard");
        }
        model.addAttribute("pageTitle", "Registration - Company name");
        model.addAttribute("user", new UserDto());
        return "auth/register"; // The register form view
    }

    // import org.springframework.validation.BindingResult;
    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("user") UserDto userDto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        // Check if there are any validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("user",userDto);
            // Return the registration form with validation error messages
            return "auth/register";
        }

        try {
            userService.registerUser(userDto);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in.");
            return "redirect:/auth/register?success"; // Redirect after successful registration
        } catch (Exception e) {
            // Handle different validation exceptions
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register"; // Return to register form with error message
        }
    }

    // Login Page
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(name = "loginError", required = false) Boolean loginError, 
                                Model model, HttpServletRequest request, HttpServletResponse response) {
        if (loginError != null && loginError) {
            model.addAttribute("loginError", "Invalid email or password.");
        }
        if (SecurityUtil.isAuthenticated()) {
            return SecurityUtil.handleAuthenticatedUserRedirect(request, response, "/auth/dashboard");
        }
        model.addAttribute("pageTitle", "Login - Company name");
        return "auth/login"; // The login form view
    }

    // Dashboard (requires authentication)
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User securityUser = 
            (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        
        // Fetch the custom User entity from the database
        User user = userService.findByEmail(securityUser.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        model.addAttribute("pageTitle", "Dashboard - Company name");
        model.addAttribute("user", user); // Pass user details to the dashboard
        return "dashboard/dashboard";
    }
}