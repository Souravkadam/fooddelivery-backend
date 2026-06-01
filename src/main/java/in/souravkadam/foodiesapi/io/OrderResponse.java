package in.souravkadam.foodiesapi.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private String id;
    private String userId;
    private String userAddress;
    private String phoneNumber;
    private String email;
    private double amount;
    private String paymentStatus;
    private String razorpayOrderId;
    private String orderStatus;
    private List<OrderItem> orderedItems;
    private LocalDateTime createdAt;   // ← ADDED: needed for dashboard charts
}
