package in.souravkadam.foodiesapi.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "carts")
public class CartEntity {

    @Id
    private String id;

    private String userId;

    // BUG FIX 3: field-level default is ignored by Lombok @AllArgsConstructor.
    // Use @Builder.Default so the builder always initialises items to an empty map.
    @Builder.Default
    private Map<String, Integer> items = new HashMap<>();
}
