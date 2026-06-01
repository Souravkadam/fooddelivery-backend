package in.souravkadam.foodiesapi.controller;

import in.souravkadam.foodiesapi.Entity.LoginHistory;
import in.souravkadam.foodiesapi.Entity.UserEntity;
import in.souravkadam.foodiesapi.io.AuthenticationRequest;
import in.souravkadam.foodiesapi.io.AuthenticationResponse;
import in.souravkadam.foodiesapi.io.UserRequest;
import in.souravkadam.foodiesapi.io.UserResponse;
import in.souravkadam.foodiesapi.repository.UserRepository;
import in.souravkadam.foodiesapi.service.AppUserDetailsService;
import in.souravkadam.foodiesapi.service.UserService;
import in.souravkadam.foodiesapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    // ── Register ──────────────────────────────────────────────────────────────
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody UserRequest request) {
        return userService.registorUser(request);
    }

    // ── Login ─────────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request,
                                   HttpServletRequest httpRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()));

            UserDetails userDetails =
                    appUserDetailsService.loadUserByUsername(request.getEmail());
            String token = jwtUtil.generateToken(userDetails);

            // Fetch full user entity
            UserEntity user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow();

            // Block check
            if ("BLOCKED".equals(user.getAccountStatus())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Your account has been blocked. Please contact support.");
            }

            // Update login tracking
            user.setLastLogin(LocalDateTime.now());
            user.setLoginCount(user.getLoginCount() + 1);

            // Record login history
            if (user.getLoginHistory() == null) {
                user.setLoginHistory(new ArrayList<>());
            }
            LoginHistory history = LoginHistory.builder()
                    .loginTime(LocalDateTime.now())
                    .ipAddress(getClientIp(httpRequest))
                    .device(httpRequest.getHeader("User-Agent"))
                    .browser(parseBrowser(httpRequest.getHeader("User-Agent")))
                    .status("SUCCESS")
                    .build();
            user.getLoginHistory().add(0, history);
            if (user.getLoginHistory().size() > 20) {
                user.getLoginHistory().subList(20, user.getLoginHistory().size()).clear();
            }
            userRepository.save(user);

            return ResponseEntity.ok(
                    AuthenticationResponse.builder()
                            .email(user.getEmail())
                            .token(token)
                            .role(user.getRole())
                            .name(user.getName())
                            .userId(user.getId())
                            .build()
            );

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Incorrect email or password");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isEmpty()) ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }

    private String parseBrowser(String ua) {
        if (ua == null) return "Unknown";
        if (ua.contains("Chrome"))  return "Chrome";
        if (ua.contains("Firefox")) return "Firefox";
        if (ua.contains("Safari"))  return "Safari";
        if (ua.contains("Edge"))    return "Edge";
        return "Other";
    }
}
