package in.souravkadam.foodiesapi.controller;

import in.souravkadam.foodiesapi.io.FoodRequest;
import in.souravkadam.foodiesapi.io.FoodResponse;
import in.souravkadam.foodiesapi.service.FoodService;
import in.souravkadam.foodiesapi.service.FoodServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/foods")
@CrossOrigin(
        origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE,
                   RequestMethod.PUT, RequestMethod.PATCH}
)
public class FoodController {

    private final FoodService foodService;
    private final FoodServiceImpl foodServiceImpl;

    // ── GET all ───────────────────────────────────────────────────────────────
    @GetMapping
    public List<FoodResponse> readFoods() {
        return foodService.readFood();
    }

    // ── GET by ID ─────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public FoodResponse readFood(@PathVariable String id) {
        return foodService.readFood(id);
    }

    // ── POST add food ─────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> addFood(
            @RequestPart("food") String foodString,
            @RequestPart("file") MultipartFile file) {

        ObjectMapper objectMapper = new ObjectMapper();
        FoodRequest request;
        try {
            request = objectMapper.readValue(foodString, FoodRequest.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON: " + e.getMessage());
        }
        FoodResponse response = foodService.addFood(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── PUT update food ───────────────────────────────────────────────────────
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateFood(
            @PathVariable String id,
            @RequestPart(value = "food", required = false) String foodString,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        ObjectMapper objectMapper = new ObjectMapper();
        FoodRequest request = new FoodRequest();
        if (foodString != null) {
            try {
                request = objectMapper.readValue(foodString, FoodRequest.class);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid JSON: " + e.getMessage()));
            }
        }
        return ResponseEntity.ok(foodServiceImpl.updateFood(id, request, file));
    }

    // ── PATCH toggle availability ─────────────────────────────────────────────
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleAvailability(@PathVariable String id) {
        return ResponseEntity.ok(foodServiceImpl.toggleAvailability(id));
    }

    // ── DELETE food ───────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFood(@PathVariable String id) {
        foodService.deleteFood(id);
    }
}
