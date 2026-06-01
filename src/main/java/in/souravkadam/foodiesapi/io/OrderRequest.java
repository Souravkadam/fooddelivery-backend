package in.souravkadam.foodiesapi.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor          // ✅ REQUIRED for Jackson
@AllArgsConstructor         // ✅ Keeps your builder logic safe
@Builder
public class OrderRequest {

    private List<OrderItem> orderedItems;
    private String userAddress;
    private double amount;
    private String email;
    private String phoneNumber;
    private String orderStatus;
}
