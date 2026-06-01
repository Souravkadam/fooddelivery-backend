package in.souravkadam.foodiesapi.repository;

import in.souravkadam.foodiesapi.Entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {

    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findByAccountStatus(String accountStatus);

    List<UserEntity> findByRole(String role);

    long countByAccountStatus(String accountStatus);

    long countByCreatedAtAfter(LocalDateTime date);

    List<UserEntity> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String name, String email);
}
