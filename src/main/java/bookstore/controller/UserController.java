package bookstore.controller;

import bookstore.dto.ProfileDto;
import bookstore.dto.UpdatepasswordDto;
import bookstore.models.User;
import bookstore.service.UserService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/profile")
    public String getProfile(Model model) {
        Optional<User> optionalUser = userService.getCurrentUser();

        if (optionalUser.isPresent()) {
            User currentUser = optionalUser.get();
            model.addAttribute("currentUser", currentUser);
            // Log the current user's details
            logger.info("Current user: {}", currentUser);
        } else {
            // Handle the case where the user is not found
            System.out.println("Current user is null!");
        }
        model.addAttribute("pageTitle", "User profile - Company name");
        return "user/profile";
    }

    @GetMapping("/change-password")
    public String showPasswordPage(Model model, @AuthenticationPrincipal User currentUser) {
        model.addAttribute("user", currentUser);
        model.addAttribute("pageTitle", "Change Password - Company name");
        return "user/update-password";
    }

    @PostMapping("/update-password")
    public String updatePassword(@Valid @ModelAttribute("user") UpdatepasswordDto userDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Validation error occurred.");
            bindingResult.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage()));
            return "redirect:/user/change-password";
        }

        Optional<User> optionalUser = userService.getCurrentUser();
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/user/change-password";
        }

        User currentUser = optionalUser.get();

        // Check if the current password is correct
        if (!passwordEncoder.matches(userDto.getCurrentPassword(), currentUser.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect.");
            return "redirect:/user/change-password";
        }

        // Validate new password and confirm new password match
        if (!userDto.getNewPassword().equals(userDto.getConfirmNewPassword())) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/user/change-password";
        }

        // Validate password strength
        if (!userService.isPasswordStrong(userDto.getNewPassword())) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters long, include a digit, an uppercase letter, a lowercase letter, and a special character.");
            return "redirect:/user/change-password";
        }

        // Update password in the service
        userService.updatePassword(currentUser, userDto.getNewPassword());
        redirectAttributes.addFlashAttribute("message", "Password updated successfully.");
        
        // Redirect to change-password page after successful update
        return "redirect:/user/change-password";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@Valid @ModelAttribute("user") ProfileDto userDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Validation error occurred.");
            return "redirect:/user/profile";
        }
    
        User currentUser = userService.getCurrentUser()
                                      .orElseThrow(() -> new IllegalStateException("User not found."));
    
        // Update user information
        currentUser.setFirstName(userDto.getFirstName());
        currentUser.setLastName(userDto.getLastName());
    
        // Save the updated user to the database
        userService.updateUserProfile(currentUser);
        redirectAttributes.addFlashAttribute("message", "Profile updated successfully.");
        
        return "redirect:/user/profile";
    }    

}
