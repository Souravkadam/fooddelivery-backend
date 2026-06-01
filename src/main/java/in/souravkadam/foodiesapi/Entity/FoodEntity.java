package in.souravkadam.foodiesapi.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "foods")
public class FoodEntity {

    @Id
    private String id;

    private String name;
    private String description;
    private Double price;
    private String category;
    private String imageUrl;

    // ── Extended fields ────────────────────────────────────────────────────
    @Builder.Default
    private boolean available = true;

    @Builder.Default
    private int stock = 100;

    @Builder.Default
    private int totalOrders = 0;

    @Builder.Default
    private double rating = 4.5;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
