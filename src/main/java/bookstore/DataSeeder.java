package bookstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import bookstore.repository.RoleRepository;
import bookstore.service.UserService;
import bookstore.controller.ResetPasswordController;
import bookstore.dto.UserDto;
import bookstore.models.Role;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DataSeeder {

    private final RoleRepository roleRepository;
    private final UserService userService;
    
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Autowired
    public DataSeeder(RoleRepository roleRepository, UserService userService) {
        this.roleRepository = roleRepository;
        this.userService = userService;
    }

    @Bean
    public CommandLineRunner seedCategories() {
        return args -> {
            seedRolesIfNotExists();
            createAdminUser(userService, roleRepository);
        };
    }

    private void seedRolesIfNotExists() {
        List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN", "ROLE_BOOK_AUTHOR", "ROLE_BOOK_WRITER");

        for (String roleName : roles) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                System.out.println("Role '" + roleName + "' seeded.");
            } else {
                System.out.println("Role '" + roleName + "' already exists. Skipping seed.");
            }
        }
    }

    private void createAdminUser(UserService userService, RoleRepository roleRepository) {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElse(null);
        
        if (adminRole == null) {
            logger.info("Admin role not found, creating one.");
            adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);
        } else {
            logger.info("Admin role found: " + adminRole.getName());
        }
    
        // Check if the admin user already exists
        if (userService.findByEmail("admin@example.com") == null) {
            logger.info("Admin user not found, creating admin user.");
            UserDto adminUser = new UserDto();
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword("adminpassword"); // Ensure this is encoded if necessary
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
    
            Set<Role> roles = new HashSet<>(List.of(adminRole));
            adminUser.setRoles(roles);
    
            userService.registerAdminUser(adminUser);
            logger.info("Admin user created.");
        } else {
            logger.info("Admin user already exists. Skipping creation.");
        }
    }    
}