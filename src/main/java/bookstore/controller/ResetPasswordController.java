package bookstore.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import bookstore.dto.ResetPasswordRequest;
import bookstore.models.User;
import bookstore.service.UserService;
import bookstore.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/auth")
public class ResetPasswordController {
    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(ResetPasswordController.class);

    public ResetPasswordController(UserService userService) {
        this.userService = userService;
    }

    // Reset Password Page (from the token link)
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response) {
        if (SecurityUtil.isAuthenticated()) {
            return SecurityUtil.handleAuthenticatedUserRedirect(request, response, "/auth/dashboard");
        }
        System.out.println("Received token: " + token); 
        logger.info("Received reset token: {}", token);
        Optional<User> userOptional = userService.findUserByResetToken(token);
        if (userOptional.isEmpty()) {
            logger.warn("Invalid or expired token: {}", token);
            // model.addAttribute("loginError", "Invalid or expired token.");
            redirectAttributes.addFlashAttribute("loginError", "Invalid or expired token.");
            return "redirect:/auth/login";
        }
        logger.info("User found for token: {}", userOptional.get());
        model.addAttribute("token", token);
        return "auth/reset-password"; // Return the reset password form
    }

    @PostMapping("/reset-password")
    @Transactional
    public String processResetPassword(@Valid @ModelAttribute ResetPasswordRequest request, 
                                        Model model, RedirectAttributes redirectAttributes) {

        String token = request.getToken();
        String newPassword = request.getNewPassword();
        String confirmNewPassword = request.getConfirmNewPassword();
        Optional<User> userOptional = userService.findUserByResetToken(token);
        
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("loginError", "Invalid or expired token.");
            return "redirect:/auth/login";  // Redirect to login with error
        }

        if (!newPassword.equals(confirmNewPassword)) {
            logger.warn("Passwords do not match for token: {}", token);
            model.addAttribute("error", "Passwords do not match.");
            return "auth/reset-password";
        }

        // Validate password strength
        if (!userService.isPasswordStrong(newPassword)) {
            model.addAttribute("error", "Password must be at least 8 characters, include a digit, an uppercase letter, a lowercase letter, and a special character.");
            return "auth/reset-password";
        }

        try {
            // Update the user's password
            userService.updatePasswordAndDeleteToken(userOptional.get(), newPassword, token);
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully. You can now log in.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            logger.error("Error updating password for token: {}", token, e);
            model.addAttribute("error", "Error updating password: " + e.getMessage());
            return "auth/reset-password";
        }
    }
}
