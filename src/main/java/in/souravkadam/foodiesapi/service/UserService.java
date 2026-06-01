package in.souravkadam.foodiesapi.service;

import in.souravkadam.foodiesapi.io.AdminUserResponse;
import in.souravkadam.foodiesapi.io.UserRequest;
import in.souravkadam.foodiesapi.io.UserResponse;
import in.souravkadam.foodiesapi.io.UserUpdateRequest;

import java.util.List;

public interface UserService {

    UserResponse registorUser(UserRequest request);

    String findByUserId();

    // ── Admin operations ──────────────────────────────────────────────────────
    List<AdminUserResponse> getAllUsers();

    AdminUserResponse getUserById(String id);

    List<AdminUserResponse> searchUsers(String query);

    AdminUserResponse updateUser(String id, UserUpdateRequest request);

    AdminUserResponse blockUser(String id);

    AdminUserResponse unblockUser(String id);

    void deleteUser(String id);

    void resetPassword(String id, String newPassword);
}
