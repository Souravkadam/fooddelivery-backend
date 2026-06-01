package in.souravkadam.foodiesapi.Entity;

import in.souravkadam.foodiesapi.io.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id
    private String id;

    private String userId;
    private String userAddress;
    private String phoneNumber;
    private String email;

    private List<OrderItem> orderedItems;

    private double amount;
    private String paymentStatus;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String orderStatus;

    // Always set on creation — never null for new orders
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
