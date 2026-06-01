package in.souravkadam.foodiesapi.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String email;
    private String token;
    private String role;   // USER | ADMIN  — needed by admin panel
    private String name;   // display name
    private String userId;
}
