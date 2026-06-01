package in.souravkadam.foodiesapi.controller;

import in.souravkadam.foodiesapi.io.AdminUserResponse;
import in.souravkadam.foodiesapi.io.UserUpdateRequest;
import in.souravkadam.foodiesapi.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // ── Admin: get all users ──────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ── Admin: search users ───────────────────────────────────────────────────
    @GetMapping("/search")
    public ResponseEntity<List<AdminUserResponse>> searchUsers(@RequestParam String q) {
        return ResponseEntity.ok(userService.searchUsers(q));
    }

    // ── Admin: get user by ID ─────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ── Admin: update user ────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<AdminUserResponse> updateUser(@PathVariable String id,
                                                        @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // ── Admin: block user ─────────────────────────────────────────────────────
    @PatchMapping("/{id}/block")
    public ResponseEntity<AdminUserResponse> blockUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.blockUser(id));
    }

    // ── Admin: unblock user ───────────────────────────────────────────────────
    @PatchMapping("/{id}/unblock")
    public ResponseEntity<AdminUserResponse> unblockUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.unblockUser(id));
    }

    // ── Admin: reset password ─────────────────────────────────────────────────
    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable String id,
                                           @RequestBody Map<String, String> body) {
        String password = body.get("password");
        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Password must be at least 6 characters"));
        }
        userService.resetPassword(id, password);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    // ── Admin: delete user ────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}
