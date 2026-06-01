package in.souravkadam.foodiesapi.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
@Builder
public class UserEntity {

    @Id
    private String id;

    private String name;
    private String email;
    private String password;

    @Builder.Default
    private String role = "USER";                    // USER | ADMIN

    @Builder.Default
    private String accountStatus = "ACTIVE";         // ACTIVE | BLOCKED | INACTIVE

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;

    @Builder.Default
    private int loginCount = 0;

    @Builder.Default
    private int totalOrders = 0;

    @Builder.Default
    private double totalSpent = 0.0;

    private String phoneNumber;
    private String address;
    private String profileImage;

    // ── Login history (last 20 logins) ────────────────────────────────────────
    @Builder.Default
    private List<LoginHistory> loginHistory = new ArrayList<>();
}
