package in.souravkadam.foodiesapi.config;

import in.souravkadam.foodiesapi.Entity.UserEntity;
import in.souravkadam.foodiesapi.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Runs once on every startup.
 * Creates the admin account if it does not already exist in MongoDB.
 *
 * Credentials:
 *   Email    : souravkadam8080@gmail.com
 *   Password : sourav@9322
 *   Role     : ADMIN
 */
@Component
@AllArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL    = "souravkadam8080@gmail.com";
    private static final String ADMIN_PASSWORD = "sourav@9322";
    private static final String ADMIN_NAME     = "Sourav Kadam";

    @Override
    public void run(String... args) {

        // Check if admin already exists
        var existing = userRepository.findByEmail(ADMIN_EMAIL);
        if (existing.isPresent()) {
            UserEntity user = existing.get();
            // Fix role if it was created as USER before
            if (!"ADMIN".equals(user.getRole())) {
                user.setRole("ADMIN");
                user.setAccountStatus("ACTIVE");
                userRepository.save(user);
                System.out.println("✅ Admin role updated to ADMIN for: " + ADMIN_EMAIL);
            } else {
                System.out.println("✅ Admin account already exists — skipping seed.");
            }
            return;
        }

        // Create admin user
        UserEntity admin = UserEntity.builder()
                .name(ADMIN_NAME)
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .role("ADMIN")
                .accountStatus("ACTIVE")
                .createdAt(LocalDateTime.now())
                .loginCount(0)
                .totalOrders(0)
                .totalSpent(0.0)
                .build();

        userRepository.save(admin);

        System.out.println("========================================");
        System.out.println("✅ Admin account created successfully!");
        System.out.println("   Email    : " + ADMIN_EMAIL);
        System.out.println("   Password : " + ADMIN_PASSWORD);
        System.out.println("   Role     : ADMIN");
        System.out.println("========================================");
    }
}
