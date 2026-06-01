package in.souravkadam.foodiesapi.service;

import in.souravkadam.foodiesapi.Entity.FoodEntity;
import in.souravkadam.foodiesapi.io.FoodRequest;
import in.souravkadam.foodiesapi.io.FoodResponse;
import in.souravkadam.foodiesapi.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FoodServiceImpl implements FoodService {

    private final S3Client s3Client;
    private final FoodRepository foodRepository;

    @Value("${aws.s3.bucketName:foodies-foods-s1}")
    private String bucketName;

    @Value("${storage.local:true}")
    private boolean useLocalStorage;

    @Value("${storage.local.path:uploads}")
    private String localUploadPath;

    @Autowired
    public FoodServiceImpl(S3Client s3Client, FoodRepository foodRepository) {
        this.s3Client = s3Client;
        this.foodRepository = foodRepository;
    }

    // ── Upload: converts image to base64 data URL stored in MongoDB ───────────
    @Override
    public String uploadFile(MultipartFile file) {
        if (useLocalStorage) {
            return convertToBase64DataUrl(file);
        }
        return uploadFileToS3(file);
    }

    /**
     * Converts the uploaded image to a base64 data URL.
     * This is stored directly in MongoDB as the imageUrl field.
     * No file system or S3 needed — works everywhere.
     * Format: "data:image/jpeg;base64,/9j/4AAQ..."
     */
    private String convertToBase64DataUrl(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String contentType = file.getContentType();
            if (contentType == null) contentType = "image/jpeg";
            String dataUrl = "data:" + contentType + ";base64," + base64;
            System.out.println("Image converted to base64, size: " + bytes.length + " bytes");
            return dataUrl;
        } catch (IOException e) {
            System.err.println("Base64 conversion error: " + e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Image processing failed: " + e.getMessage(), e);
        }
    }

    private String uploadFileToS3(MultipartFile file) {
        String key = generateUniqueFilename(file);
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .acl("public-read")
                    .contentType(file.getContentType())
                    .build();

            PutObjectResponse response = s3Client.putObject(
                    putRequest, RequestBody.fromBytes(file.getBytes()));

            if (response.sdkHttpResponse().isSuccessful()) {
                return "https://" + bucketName + ".s3.amazonaws.com/" + key;
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 upload failed");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 upload error", e);
        }
    }

    private String generateUniqueFilename(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original == null || !original.contains(".")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }
        String ext = original.substring(original.lastIndexOf('.') + 1);
        return UUID.randomUUID() + "." + ext;
    }

    // ── Delete ────────────────────────────────────────────────────────────────
    @Override
    public boolean deleteFile(String filename) {
        if (useLocalStorage) {
            // Base64 images are stored in MongoDB — nothing to delete from disk
            return true;
        }
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName).key(filename).build());
        return true;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────
    @Override
    public FoodResponse addFood(FoodRequest request, MultipartFile file) {
        System.out.println("Adding food: " + request.getName());
        FoodEntity food = convertToEntity(request);
        food.setImageUrl(uploadFile(file));
        food = foodRepository.save(food);
        System.out.println("Food saved with id: " + food.getId());
        return convertToResponse(food);
    }

    @Override
    public List<FoodResponse> readFood() {
        return foodRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public FoodResponse readFood(String id) {
        return foodRepository.findById(id)
                .map(this::convertToResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Food not found: " + id));
    }

    @Override
    public void deleteFood(String id) {
        FoodResponse food = readFood(id);
        // Only try to delete file if it's an S3 URL (not base64)
        if (!useLocalStorage
                && food.getImageUrl() != null
                && !food.getImageUrl().startsWith("data:")) {
            String filename = food.getImageUrl()
                    .substring(food.getImageUrl().lastIndexOf('/') + 1);
            deleteFile(filename);
        }
        foodRepository.deleteById(food.getId());
    }

    // ── Update food ───────────────────────────────────────────────────────────
    public FoodEntity updateFood(String id, FoodRequest request, MultipartFile file) {
        FoodEntity food = foodRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found: " + id));

        if (request.getName() != null)        food.setName(request.getName());
        if (request.getDescription() != null) food.setDescription(request.getDescription());
        if (request.getPrice() != null)       food.setPrice(request.getPrice());
        if (request.getCategory() != null)    food.setCategory(request.getCategory());

        if (file != null && !file.isEmpty()) {
            food.setImageUrl(uploadFile(file));
        }
        return foodRepository.save(food);
    }

    // ── Toggle availability ───────────────────────────────────────────────────
    public FoodEntity toggleAvailability(String id) {
        FoodEntity food = foodRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found: " + id));
        food.setAvailable(!food.isAvailable());
        return foodRepository.save(food);
    }

    // ── Converters ────────────────────────────────────────────────────────────
    private FoodEntity convertToEntity(FoodRequest req) {
        return FoodEntity.builder()
                .name(req.getName())
                .description(req.getDescription())
                .category(req.getCategory())
                .price(req.getPrice())
                .build();
    }

    private FoodResponse convertToResponse(FoodEntity e) {
        return FoodResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .category(e.getCategory())
                .price(e.getPrice())
                .imageUrl(e.getImageUrl())
                .build();
    }
}
