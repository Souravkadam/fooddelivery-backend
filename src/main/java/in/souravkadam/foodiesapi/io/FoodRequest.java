package in.souravkadam.foodiesapi.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodRequest {

    private String name;
    private String description;
    private Double price;
    private String category;


}
