package in.souravkadam.foodiesapi.repository;

import in.souravkadam.foodiesapi.Entity.OrderEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<OrderEntity, String> {

    List<OrderEntity> findByUserId(String userId);

    Optional<OrderEntity> findByRazorpayOrderId(String razorpayOrderId);

    List<OrderEntity> findByOrderStatus(String orderStatus);

    List<OrderEntity> findByPaymentStatus(String paymentStatus);

    List<OrderEntity> findByCreatedAtAfter(LocalDateTime date);

    long countByOrderStatus(String orderStatus);

    long countByPaymentStatus(String paymentStatus);
}
