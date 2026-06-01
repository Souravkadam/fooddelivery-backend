package in.souravkadam.foodiesapi.io;

import in.souravkadam.foodiesapi.Entity.LoginHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserResponse {
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String profileImage;
    private String role;
    private String accountStatus;
    private LocalDateTime lastLogin;
    private int loginCount;
    private int totalOrders;
    private double totalSpent;
    private LocalDateTime createdAt;
    private List<LoginHistory> loginHistory;
}
