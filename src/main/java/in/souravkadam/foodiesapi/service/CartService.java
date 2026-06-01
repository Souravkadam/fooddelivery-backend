package in.souravkadam.foodiesapi.service;


import in.souravkadam.foodiesapi.io.CartReponse;
import in.souravkadam.foodiesapi.io.CartRequest;

public interface CartService {

   CartReponse addToCart(CartRequest request);

   CartReponse getCart();

   void clearCart();

   CartReponse removeFromCart(CartRequest cartrequest);
}
