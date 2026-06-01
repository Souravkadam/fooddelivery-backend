package in.souravkadam.foodiesapi.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistory {
    private LocalDateTime loginTime;
    private String ipAddress;
    private String device;
    private String browser;
    private String status; // SUCCESS | FAILED
}
