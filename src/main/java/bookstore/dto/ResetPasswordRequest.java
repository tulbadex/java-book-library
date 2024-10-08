package bookstore.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ResetPasswordRequest {
    @NotEmpty(message = "Token should not be empty")
    private String token;
    @NotEmpty(message = "New password field should not be empty")
    private String newPassword;
    @NotEmpty(message = "Confirm new password field should not be empty")
    private String confirmNewPassword;
}
