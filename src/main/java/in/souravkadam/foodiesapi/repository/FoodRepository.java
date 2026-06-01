package in.souravkadam.foodiesapi.repository;

import in.souravkadam.foodiesapi.Entity.FoodEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends MongoRepository<FoodEntity, String> {

    List<FoodEntity> findByCategory(String category);

    List<FoodEntity> findByAvailable(boolean available);

    List<FoodEntity> findByNameContainingIgnoreCase(String name);

    long countByCategory(String category);

    long countByAvailable(boolean available);
}
