package in.souravkadam.foodiesapi.repository;

import in.souravkadam.foodiesapi.Entity.CartEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRespository extends MongoRepository<CartEntity,String> {
   Optional<CartEntity> findByUserId(String userId);

   void deleteByUserId(String UserId);
}
