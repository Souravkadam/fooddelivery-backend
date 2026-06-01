package in.souravkadam.foodiesapi.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        System.err.println("ResponseStatusException: " + ex.getReason());
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(Map.of(
                        "error", ex.getReason() != null ? ex.getReason() : "Error",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "File too large",
                        "message", "Image must be under 10MB"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        System.err.println("Unhandled exception: " + ex.getClass().getName() + ": " + ex.getMessage());
        ex.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", ex.getClass().getSimpleName(),
                        "message", ex.getMessage() != null ? ex.getMessage() : "Unknown error"
                ));
    }
}
