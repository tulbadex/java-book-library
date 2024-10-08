package bookstore.controller;

import bookstore.models.User;
import bookstore.service.UserService;
import bookstore.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class ForgotPasswordController {
    private final UserService userService;

    public ForgotPasswordController(UserService userService) {
        this.userService = userService;
    }

    // Forgot password page
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(HttpServletRequest request, HttpServletResponse response) {
        if (SecurityUtil.isAuthenticated()) {
            return SecurityUtil.handleAuthenticatedUserRedirect(request, response, "/auth/dashboard");
        }
        return "auth/forgot-password";
    }

    // Process forgot password request
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email,  HttpServletRequest request, Model model) {
        User user = userService.findByEmail(email).orElse(null);
        if (user == null) {
            model.addAttribute("error", "User not found with this email address");
            return "auth/forgot-password";
        }

        try {
            // Pass the HttpServletRequest to sendPasswordResetLink
            userService.sendPasswordResetLink(user.getEmail(), request);
            model.addAttribute("message", "Password reset link sent to your email.");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to send password reset email. Please try again later.");
        }

        return "auth/forgot-password";
    }
}
