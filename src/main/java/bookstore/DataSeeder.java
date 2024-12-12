package bookstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import bookstore.repository.CategoryRepository;
import bookstore.repository.RoleRepository;
import bookstore.service.UserService;
import bookstore.dto.UserDto;
import bookstore.models.Category;
import bookstore.models.Role;
import bookstore.models.User;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DataSeeder {

    private final RoleRepository roleRepository;
    private final UserService userService;
    private final CategoryRepository categoryRepository;

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Autowired
    public DataSeeder(RoleRepository roleRepository, UserService userService, CategoryRepository categoryRepository) {
        this.roleRepository = roleRepository;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
    }

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            seedRolesIfNotExists();
            createAdminUser();
            seedCategoryIfNotExists();
        };
    }

    private void seedCategoryIfNotExists() {
        List<String> categories = Arrays.asList(
            "Fantasy", 
            "Science Fiction", 
            "Action & Adventure", 
            "Mystery",
            "Horror",
            "Thriller & Suspense",
            "Historical Fiction",
            "Romance",
            "Women’s Fiction",
            "LGBTQ+",
            "Contemporary Fiction",
            "Literary Fiction",
            "Magical Realism",
            "Graphic Novel",
            "Short Story",
            "Young Adult",
            "New Adult",
            "Children’s",
            // non fiction genres
            "Memoir & Autobiography",
            "Biography",
            "Food & Drink",
            "Art & Photography",
            "Self-help",
            "History",
            "Travel",
            "True Crime",
            "Humor",
            "Essays",
            "Guide/How-to",
            "Religion & Spirituality",
            "Humanities & Social Sciences",
            "Parenting & Families",
            "Science & Technology",
            "Children’s"
        );

        // Drop all existing categories
        // categoryRepository.deleteAll();

        for (var catName : categories) {
            Optional<Category> existingCategory = categoryRepository.findByName(catName);
            if(existingCategory.isEmpty()) {
                Category category = new Category();
                category.setName(catName);
                categoryRepository.save(category);
            } else {
                System.out.println("Category '" + catName + "' already exists. Skipping seed.");
            }
        }
    }

    private void seedRolesIfNotExists() {
        List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN", "ROLE_BOOK_AUTHOR", "ROLE_BOOK_WRITER");

        for (String roleName : roles) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                logger.info("Role '{}' seeded.", roleName);
            } else {
                logger.info("Role '{}' already exists. Skipping seed.", roleName);
            }
        }
    }

    private void createAdminUser() {
        Optional<Role> adminRoleOpt = roleRepository.findByName("ROLE_ADMIN");
        Role adminRole;

        if (adminRoleOpt.isEmpty()) {
            logger.info("Admin role not found, creating one.");
            adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            adminRole = roleRepository.save(adminRole);
        } else {
            adminRole = adminRoleOpt.get();
        }

        // Ensure admin role is managed
        adminRole = roleRepository.findById(adminRole.getId()).orElseThrow(() -> new IllegalStateException("Admin role not found after creation"));

        // Check if admin user exists
        Optional<User> checkEmail = userService.findByEmail("admin@example.com");
        if (checkEmail.isEmpty()) {
            logger.info("Admin user not found, creating one.");

            // Create the admin user DTO
            UserDto adminUser = new UserDto();
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword("adminpassword"); // Replace with a secure password
            adminUser.setUsername("adminUser"); // Replace with a secure password
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");

            // Assign the admin role
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            adminUser.setRoles(roles);

            userService.registerAdminUser(adminUser);
            logger.info("Admin user created.");
        } else {
            logger.info("Admin user already exists. Skipping creation.");
        }
    }
}