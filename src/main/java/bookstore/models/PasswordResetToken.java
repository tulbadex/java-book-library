package bookstore.models;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String token;

    @Column(name = "user_id")
    private UUID userId; // This should match the User ID type

    private Date expiryDate;

    public PasswordResetToken() {
    }

    public PasswordResetToken(String token, UUID userId, Date expiryDate) {
        this.token = token;
        this.userId = userId;
        this.expiryDate = expiryDate;
    }

    public UUID getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}
