package bookstore.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdatepasswordDto {
    private UUID id;

    @NotEmpty(message = "Current password should not be empty")
    private String currentPassword;
    @NotEmpty(message = "New password should not be empty")
    private String newPassword;
    @NotEmpty(message = "Confirm new password should not be empty")
    private String confirmNewPassword;
}
