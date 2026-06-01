package in.souravkadam.foodiesapi.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsResponse {

    // User stats
    private long totalUsers;
    private long activeUsers;
    private long blockedUsers;
    private long newUsersToday;
    private long newUsersThisMonth;

    // Food stats
    private long totalFoodItems;
    private long totalCategories;

    // Order stats
    private long totalOrders;
    private long pendingOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private long outForDeliveryOrders;

    // Revenue
    private double totalRevenue;
    private double monthlyRevenue;
    private double todayRevenue;

    // Charts
    private List<Map<String, Object>> monthlyRevenueChart;
    private List<Map<String, Object>> monthlyOrdersChart;
    private List<Map<String, Object>> monthlyUsersChart;
    private List<Map<String, Object>> topSellingFoods;
    private List<Map<String, Object>> topCategories;
    private List<Map<String, Object>> topCustomers;
}
