package in.souravkadam.foodiesapi;

import in.souravkadam.foodiesapi.Entity.FoodEntity;
import in.souravkadam.foodiesapi.io.FoodResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class FoodiesapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodiesapiApplication.class, args);
	}

}
