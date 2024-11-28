package bookstore.service;

import bookstore.dto.UserDto;
import bookstore.models.PasswordResetToken;
import bookstore.models.Role;
import bookstore.models.User;
import bookstore.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import bookstore.repository.RoleRepository;
import bookstore.repository.PasswordResetTokenRepository;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private JavaMailSender mailSender;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User registerUser(UserDto userDto) throws Exception {
        // Validate email
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new Exception("This email is already registered.");
        }

        // Validate username
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new Exception("This username is already taken.");
        }

        // Validate first name
        if (userDto.getFirstName() == null) {
            throw new Exception("First name is required.");
        }

        // Validate last name
        if (userDto.getLastName() == null) {
            throw new Exception("Last name is required.");
        }

        // Validate username length
        if (userDto.getUsername().length() < 3 || userDto.getUsername().length() > 20) {
            throw new Exception("Username must be between 3 and 20 characters.");
        }

        if (userDto.getFirstName().length() < 3 || userDto.getFirstName().length() > 20) {
            throw new Exception("First name must be between 3 and 20 characters.");
        }

        if (userDto.getLastName().length() < 3 || userDto.getLastName().length() > 20) {
            throw new Exception("Last name must be between 3 and 20 characters.");
        }

        // Validate password strength
        if (!isPasswordStrong(userDto.getPassword())) {
            throw new Exception("Password must be at least 8 characters, include a digit, an uppercase letter, a lowercase letter, and a special character.");
        }

        // Encrypt password and assign default roles
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setUsersname(userDto.getUsername());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEnabled(true);  // Enable the user
        // Assign default role to user
        user.setRoles(getDefaultRoles());
        
        return saveUser(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Async
    public void sendPasswordResetLink(String email, HttpServletRequest request) throws MessagingException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("No user found with this email"));
    
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user.getId(), calculateExpiryDate()); // 1 hour expiry
        tokenRepository.save(resetToken);
    
        // String resetUrl = "http://localhost:8080/auth/reset-password?token=" + token;

        // Build the reset URL dynamically
        String domain = request.getScheme() + "://" + request.getServerName();
        if (request.getServerPort() != 80 && request.getServerPort() != 443) {
            domain += ":" + request.getServerPort();
        }
        String resetUrl = domain + "/auth/reset-password?token=" + token;

        // Prepare email template with dynamic values
        // Prepare email template with dynamic values
        LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        String year = String.valueOf(currentDate.getYear()); // Current year
        String emailContent = buildEmailTemplate(resetUrl, year);

        // Send email using MimeMessage
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(user.getEmail());
        helper.setSubject("Password Reset Request");
        helper.setText(emailContent, true);  // true for HTML
        
        // Send email with better error handling
        try {
            // SimpleMailMessage mailMessage = new SimpleMailMessage();
            // mailMessage.setTo(user.getEmail());
            // mailMessage.setSubject("Password Reset Request");
            // mailMessage.setText("Click the following link to reset your password: " + resetUrl);
            mailSender.send(message);
        } catch (Exception e) {
            // Log the error or handle it accordingly
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
 
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Update user profile
    public void updateUserProfile(User user) {
        userRepository.save(user);  // Just saving the user object updates the profile
    }

    // Update user's password and delete the reset token
    @Transactional
    public void updatePasswordAndDeleteToken(User user, String newPassword, String token) {
        // Encode the new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete the token after successful password update
        tokenRepository.deleteByToken(token);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getCurrentUser() {
        // Get the authentication object from the security context
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    
        // Check if the principal is an instance of UserDetails
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            // Fetch the user details from the database using the username
            return userRepository.findByEmail(username);
        } else {
            // Handle the case where the principal is not an instance of UserDetails
            return Optional.empty();
        }
    }    

    // Check if password meets the complexity requirements
    public boolean isPasswordStrong(String password) {
        return password.matches("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}");
    }

    public Set<Role> getDefaultRoles() {
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("ROLE_USER")
                          .orElseThrow(() -> new RuntimeException("Role not found"));
        roles.add(userRole);
        return roles;
    }

    private Date calculateExpiryDate() {
        // Set the expiration time to 24 hours from now
        // long ONE_DAY_IN_MILLISECONDS = 86400000L;
        // return new Date(System.currentTimeMillis() + ONE_DAY_IN_MILLISECONDS);
        // set the expiration time to 1 hour
        return new Date(System.currentTimeMillis() + 3600000);
    }

    public Optional<User> findUserByResetToken(String token) {
        // Fetch the token from the repository
        System.out.println("Token from find user by token: " + token);
        Optional<PasswordResetToken> passwordResetToken = tokenRepository.findByToken(token);
        if (passwordResetToken.isPresent()) {
            PasswordResetToken resetToken = passwordResetToken.get();
            // Check if the token has expired
            if (resetToken.getExpiryDate().after(new Date())) {
                // Token is valid, return the associated user
                return userRepository.findById(resetToken.getUserId());
            } else {
                // Token has expired, delete it
                tokenRepository.delete(resetToken);
            }
        }
        // Token is invalid or expired
        return Optional.empty();
    }

    // public void registerAdminUser(UserDto userDto) {
    //     User user = new User();
    //     user.setEmail(userDto.getEmail());
    //     user.setFirstName(userDto.getFirstName());
    //     user.setLastName(userDto.getLastName());
    //     user.setPassword(passwordEncoder.encode(userDto.getPassword()));
    
    //     // Assign the admin role
    //     Role adminRole = roleRepository.findByName("ROLE_ADMIN").get();
    //     user.setRoles(Collections.singleton(adminRole));
    
    //     userRepository.save(user);
    // }

    // UserService method to register admin user
    @Transactional
    public void registerAdminUser(UserDto adminUserDto) {
        User adminUser = new User();
        adminUser.setEmail(adminUserDto.getEmail());
        adminUser.setUsername(adminUserDto.getUsername()); // Set the username to the email or another unique value
        adminUser.setPassword(passwordEncoder.encode(adminUserDto.getPassword())); // Encode the password
        adminUser.setFirstName(adminUserDto.getFirstName());
        adminUser.setLastName(adminUserDto.getLastName());

        // Fetch the roles from the database to ensure they are managed entities
        Set<Role> managedRoles = new HashSet<>();
        for (Role role : adminUserDto.getRoles()) {
            Role managedRole = roleRepository.findById(role.getId()).orElseThrow(() -> new IllegalStateException("Role not found: " + role.getName()));
            managedRoles.add(managedRole);
        }
        adminUser.setRoles(managedRoles);

        logger.info("Registering admin user: {}", adminUser);
        userRepository.save(adminUser); // Save the user to the database
    }

    // Helper method to build the HTML email content
    private String buildEmailTemplate(String resetUrl, String year) {
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta http-equiv="X-UA-Compatible" content="IE=edge">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Password Reset</title>
            <style>
                @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600&display=swap');
                * { font-family: 'Inter', sans-serif; }
                body { background-color: #f8fafc; margin: 0; padding: 0; }
                .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 8px; }
                .header { text-align: center; padding: 20px; }
                .content { padding: 20px; color: #333333; font-size: 16px; line-height: 1.6; }
                .btn { display: inline-block; background-color: #3490dc; color: #ffffff; padding: 10px 20px; border-radius: 5px; text-decoration: none; font-weight: 600; }
                .footer { text-align: center; padding: 20px; font-size: 12px; color: #999999; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Password Reset</h1>
                </div>
                <div class="content">
                    <p>Hello,</p>
                    <p>We received a request to reset your password. Click the button below to reset your password:</p>
                    <p>
                        <a href="%s" class="btn">Reset Password</a>
                    </p>
                    <p>If you did not request this, please ignore this email.</p>
                    <p>Thank you!</p>
                </div>
                <div class="footer">
                    <p>&copy; %s Your Company. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(resetUrl, year);
    }
}
