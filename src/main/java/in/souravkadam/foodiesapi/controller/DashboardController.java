package in.souravkadam.foodiesapi.controller;

import in.souravkadam.foodiesapi.io.DashboardStatsResponse;
import in.souravkadam.foodiesapi.service.DashboardService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
@CrossOrigin(
        origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET}
)
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }
}
