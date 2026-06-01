package in.souravkadam.foodiesapi.service;

import in.souravkadam.foodiesapi.Entity.CartEntity;
import in.souravkadam.foodiesapi.io.CartReponse;
import in.souravkadam.foodiesapi.io.CartRequest;
import in.souravkadam.foodiesapi.repository.CartRespository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRespository cartRespository;
    private final UserService userService;

    @Override
    public CartReponse addToCart(CartRequest request) {
        String loggedInUserId = userService.findByUserId();

        CartEntity cart = cartRespository.findByUserId(loggedInUserId)
                .orElseGet(() -> CartEntity.builder()
                        .userId(loggedInUserId)
                        .items(new HashMap<>())
                        .build());

        Map<String, Integer> cartItems = cart.getItems();
        cartItems.put(request.getFoodId(),
                cartItems.getOrDefault(request.getFoodId(), 0) + 1);

        cart.setItems(cartItems);
        cart = cartRespository.save(cart);

        return convertToResponse(cart);
    }

    @Override
    public CartReponse getCart() {
        String loggedInUserId = userService.findByUserId();

        // BUG FIX 1: orElseGet was calling 3-arg constructor (id, userId, items)
        // but CartEntity only has a 2-arg constructor (userId, items).
        // Using builder is cleaner and avoids constructor confusion.
        CartEntity entity = cartRespository.findByUserId(loggedInUserId)
                .orElseGet(() -> CartEntity.builder()
                        .userId(loggedInUserId)
                        .items(new HashMap<>())
                        .build());

        return convertToResponse(entity);
    }

    @Override
    public void clearCart() {
        String loggedInUserId = userService.findByUserId();
        cartRespository.deleteByUserId(loggedInUserId);
    }

    @Override
    public CartReponse removeFromCart(CartRequest cartRequest) {
        String loggedInUserId = userService.findByUserId();

        CartEntity entity = cartRespository.findByUserId(loggedInUserId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));

        Map<String, Integer> cartItems = entity.getItems();
        String foodId = cartRequest.getFoodId();

        // BUG FIX 2: was throwing "Food item not found in cart" even when
        // the item simply had qty 0 or was already removed — just ignore gracefully
        if (!cartItems.containsKey(foodId)) {
            return convertToResponse(entity); // nothing to remove, return current cart
        }

        int currentQty = cartItems.get(foodId);
        if (currentQty > 1) {
            cartItems.put(foodId, currentQty - 1);
        } else {
            cartItems.remove(foodId);
        }

        entity.setItems(cartItems);
        entity = cartRespository.save(entity);

        return convertToResponse(entity);
    }

    private CartReponse convertToResponse(CartEntity cartEntity) {
        return CartReponse.builder()
                .id(cartEntity.getId())
                .userId(cartEntity.getUserId())
                .items(cartEntity.getItems() != null ? cartEntity.getItems() : new HashMap<>())
                .build();
    }
}
