package in.souravkadam.foodiesapi.io;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private String foodId;
    private double price;

    // Frontend sends "quantities" — accept both field names
    @JsonAlias("quantities")
    private Integer quantity;

    private String category;
    private String imageUrl;
    private String description;
    private String name;
}
