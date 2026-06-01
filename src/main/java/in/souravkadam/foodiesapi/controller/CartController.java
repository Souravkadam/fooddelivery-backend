package in.souravkadam.foodiesapi.controller;

import in.souravkadam.foodiesapi.io.CartReponse;
import in.souravkadam.foodiesapi.io.CartRequest;
import in.souravkadam.foodiesapi.service.CartService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cart")   // FIX: was missing leading slash
@AllArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public CartReponse addToCart(@RequestBody CartRequest request) {
        String foodId = request.getFoodId();
        if (foodId == null || foodId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "foodId is required");
        }
        return cartService.addToCart(request);
    }

    @GetMapping
    public CartReponse getCart() {
        // FIX: log the actual authenticated user so you can see if auth is set
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("GET CART CONTROLLER HIT — principal: "
                + (auth != null ? auth.getName() : "NULL")
                + " | authenticated: "
                + (auth != null ? auth.isAuthenticated() : false));

        return cartService.getCart();
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart() {
        cartService.clearCart();
    }

    @PostMapping("/remove")
    public CartReponse removeFromCart(@RequestBody CartRequest request) {
        String foodId = request.getFoodId();
        if (foodId == null || foodId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "foodId is required");
        }
        return cartService.removeFromCart(request);
    }
}
