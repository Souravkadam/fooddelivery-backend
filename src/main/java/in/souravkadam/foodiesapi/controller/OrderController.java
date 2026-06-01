package in.souravkadam.foodiesapi.controller;

import com.razorpay.RazorpayException;
import in.souravkadam.foodiesapi.io.OrderRequest;
import in.souravkadam.foodiesapi.io.OrderResponse;
import in.souravkadam.foodiesapi.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
@CrossOrigin(
        origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE,
                   RequestMethod.PUT, RequestMethod.PATCH}
)
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrderWithPayment(@RequestBody OrderRequest request) throws RazorpayException {
       OrderResponse response = orderService.createOrderWithPayment(request);
       return response;
    }

    @PostMapping("/verify")
    public void verifyPayment(@RequestBody Map<String, String> paymentData) throws RazorpayException {
       orderService.verifyPayment(paymentData,"paid");
    }

    @GetMapping
    public List<OrderResponse> getOrders(){
        return orderService.getUserOrders();
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable String orderId){
       orderService.removeOrder(orderId);
    }
    //admin panel
    @GetMapping("/all")
    public List<OrderResponse> getOrdersOfAllUsers(){
      return orderService.getOrdersOfAllUsers();
    }
   //admin panel
    @PatchMapping("/status/{orderId}")
    public void updateOrderStatus(@PathVariable String orderId,@RequestParam String status){
        orderService.updateOrderStatus(orderId,status);
    }
}
