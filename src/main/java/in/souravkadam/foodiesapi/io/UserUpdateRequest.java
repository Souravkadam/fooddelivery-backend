package in.souravkadam.foodiesapi.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String profileImage;
    private String accountStatus;
    private String role;
}
