package in.souravkadam.foodiesapi.service;

import in.souravkadam.foodiesapi.Entity.UserEntity;
import in.souravkadam.foodiesapi.io.AdminUserResponse;
import in.souravkadam.foodiesapi.io.UserRequest;
import in.souravkadam.foodiesapi.io.UserResponse;
import in.souravkadam.foodiesapi.io.UserUpdateRequest;
import in.souravkadam.foodiesapi.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationFacade authenticationFacade;

    // ── Register ──────────────────────────────────────────────────────────────
    @Override
    public UserResponse registorUser(UserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        UserEntity newUser = UserEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .accountStatus("ACTIVE")
                .build();
        newUser = userRepository.save(newUser);
        return convertToResponse(newUser);
    }

    // ── Get logged-in user ID ─────────────────────────────────────────────────
    @Override
    public String findByUserId() {
        Authentication auth = authenticationFacade.getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: " + email))
                .getId();
    }

    // ── Admin: get all users ──────────────────────────────────────────────────
    @Override
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToAdminResponse)
                .collect(Collectors.toList());
    }

    // ── Admin: get user by ID ─────────────────────────────────────────────────
    @Override
    public AdminUserResponse getUserById(String id) {
        return userRepository.findById(id)
                .map(this::convertToAdminResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: " + id));
    }

    // ── Admin: search users ───────────────────────────────────────────────────
    @Override
    public List<AdminUserResponse> searchUsers(String query) {
        return userRepository
                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query)
                .stream()
                .map(this::convertToAdminResponse)
                .collect(Collectors.toList());
    }

    // ── Admin: update user ────────────────────────────────────────────────────
    @Override
    public AdminUserResponse updateUser(String id, UserUpdateRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: " + id));

        if (request.getName() != null)          user.setName(request.getName());
        if (request.getEmail() != null)         user.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null)   user.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null)       user.setAddress(request.getAddress());
        if (request.getProfileImage() != null)  user.setProfileImage(request.getProfileImage());
        if (request.getAccountStatus() != null) user.setAccountStatus(request.getAccountStatus());
        if (request.getRole() != null)          user.setRole(request.getRole());

        return convertToAdminResponse(userRepository.save(user));
    }

    // ── Admin: block user ─────────────────────────────────────────────────────
    @Override
    public AdminUserResponse blockUser(String id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: " + id));
        user.setAccountStatus("BLOCKED");
        return convertToAdminResponse(userRepository.save(user));
    }

    // ── Admin: unblock user ───────────────────────────────────────────────────
    @Override
    public AdminUserResponse unblockUser(String id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: " + id));
        user.setAccountStatus("ACTIVE");
        return convertToAdminResponse(userRepository.save(user));
    }

    // ── Admin: delete user ────────────────────────────────────────────────────
    @Override
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    // ── Admin: reset password ─────────────────────────────────────────────────
    @Override
    public void resetPassword(String id, String newPassword) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: " + id));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ── Converters ────────────────────────────────────────────────────────────
    private UserResponse convertToResponse(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    private AdminUserResponse convertToAdminResponse(UserEntity user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .profileImage(user.getProfileImage())
                .role(user.getRole())
                .accountStatus(user.getAccountStatus())
                .lastLogin(user.getLastLogin())
                .loginCount(user.getLoginCount())
                .totalOrders(user.getTotalOrders())
                .totalSpent(user.getTotalSpent())
                .createdAt(user.getCreatedAt())
                .loginHistory(user.getLoginHistory())
                .build();
    }
}
