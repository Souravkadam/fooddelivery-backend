package in.souravkadam.foodiesapi.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import in.souravkadam.foodiesapi.Entity.OrderEntity;
import in.souravkadam.foodiesapi.Entity.UserEntity;
import in.souravkadam.foodiesapi.io.OrderItem;
import in.souravkadam.foodiesapi.io.OrderRequest;
import in.souravkadam.foodiesapi.io.OrderResponse;
import in.souravkadam.foodiesapi.repository.CartRespository;
import in.souravkadam.foodiesapi.repository.FoodRepository;
import in.souravkadam.foodiesapi.repository.OrderRepository;
import in.souravkadam.foodiesapi.repository.UserRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final CartRespository cartRespository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;

    @Value("${razorpay.key:rzp_test_SkCF8SuhtObi2q}")
    private String razorpayKey;

    @Value("${razorpay.secret:dummy_secret}")
    private String razorpaySecret;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            UserService userService,
                            CartRespository cartRespository,
                            UserRepository userRepository,
                            FoodRepository foodRepository) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.cartRespository = cartRespository;
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
    }

    // ── Create order + Razorpay ───────────────────────────────────────────────
    @Override
    public OrderResponse createOrderWithPayment(OrderRequest request) throws RazorpayException {
        String loggedInUserId = userService.findByUserId();

        OrderEntity newOrder = OrderEntity.builder()
                .userId(loggedInUserId)
                .userAddress(request.getUserAddress())
                .amount(request.getAmount())
                .orderedItems(request.getOrderedItems())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .orderStatus("preparing")
                .paymentStatus("pending")
                .build();

        newOrder = orderRepository.save(newOrder);

        RazorpayClient razorpayClient = new RazorpayClient(razorpayKey, razorpaySecret);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (int) Math.round(request.getAmount() * 100));
        orderRequest.put("currency", "INR");
        orderRequest.put("payment_capture", 1);

        com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        newOrder.setRazorpayOrderId(razorpayOrder.get("id"));
        newOrder = orderRepository.save(newOrder);

        return convertToResponse(newOrder);
    }

    // ── Verify payment + update stats ─────────────────────────────────────────
    @Override
    public void verifyPayment(Map<String, String> paymentData, String status) {
        String razorpayOrderId  = paymentData.get("razorpay_order_id");
        String razorpayPaymentId = paymentData.get("razorpay_payment_id");
        String razorpaySignature = paymentData.get("razorpay_signature");

        // Verify HMAC-SHA256 signature
        if (razorpayPaymentId != null && razorpaySignature != null) {
            try {
                String payload = razorpayOrderId + "|" + razorpayPaymentId;
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(razorpaySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
                String generated = HexFormat.of().formatHex(hash);
                if (!generated.equals(razorpaySignature)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Payment signature verification failed");
                }
            } catch (ResponseStatusException e) {
                throw e;
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Signature verification error: " + e.getMessage());
            }
        }

        OrderEntity order = orderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found for razorpay id: " + razorpayOrderId));

        order.setPaymentStatus(status);
        order.setRazorpaySignature(razorpaySignature);
        order.setRazorpayPaymentId(razorpayPaymentId);
        order.setOrderStatus("confirmed");
        orderRepository.save(order);

        // Clear cart after successful payment
        if ("paid".equalsIgnoreCase(status)) {
            cartRespository.deleteByUserId(order.getUserId());
        }

        // Update user stats
        userRepository.findById(order.getUserId()).ifPresent(user -> {
            user.setTotalOrders(user.getTotalOrders() + 1);
            user.setTotalSpent(user.getTotalSpent() + order.getAmount());
            userRepository.save(user);
        });

        // Update food totalOrders
        if (order.getOrderedItems() != null) {
            order.getOrderedItems().forEach(item -> {
                if (item.getFoodId() != null) {
                    foodRepository.findById(item.getFoodId()).ifPresent(food -> {
                        int qty = item.getQuantity() != null ? item.getQuantity() : 1;
                        food.setTotalOrders(food.getTotalOrders() + qty);
                        foodRepository.save(food);
                    });
                }
            });
        }
    }

    @Override
    public List<OrderResponse> getUserOrders() {
        String loggedInUserId = userService.findByUserId();
        return orderRepository.findByUserId(loggedInUserId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void removeOrder(String orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + orderId);
        }
        orderRepository.deleteById(orderId);
    }

    @Override
    public List<OrderResponse> getOrdersOfAllUsers() {
        return orderRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void updateOrderStatus(String orderId, String status) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found: " + orderId));
        order.setOrderStatus(status);
        orderRepository.save(order);
    }

    private OrderResponse convertToResponse(OrderEntity o) {
        return OrderResponse.builder()
                .id(o.getId())
                .userId(o.getUserId())
                .userAddress(o.getUserAddress())
                .phoneNumber(o.getPhoneNumber())
                .email(o.getEmail())
                .amount(o.getAmount())
                .paymentStatus(o.getPaymentStatus())
                .razorpayOrderId(o.getRazorpayOrderId())
                .orderStatus(o.getOrderStatus())
                .orderedItems(o.getOrderedItems())
                // If old order has null createdAt, default to now so charts work
                .createdAt(o.getCreatedAt() != null ? o.getCreatedAt() : LocalDateTime.now())
                .build();
    }
}
