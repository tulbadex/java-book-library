package bookstore.dto;

import java.util.Set;
import java.util.UUID;

import bookstore.models.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDto {
    private UUID id;

    @NotEmpty(message = "Username should not be empty")
    private String username;

    @NotEmpty(message = "Email should not be empty")
    @Email
    private String email;

    @NotEmpty(message = "Password should not be empty")
    private String password;

    private Set<Role> roles;

    @NotEmpty(message = "First name should not be empty")
    private String firstName;
    @NotEmpty(message = "Last name should not be empty")
    private String lastName;

    private boolean enabled;
}
