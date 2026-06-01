package in.souravkadam.foodiesapi.service;

import in.souravkadam.foodiesapi.Entity.OrderEntity;
import in.souravkadam.foodiesapi.io.DashboardStatsResponse;
import in.souravkadam.foodiesapi.repository.FoodRepository;
import in.souravkadam.foodiesapi.repository.OrderRepository;
import in.souravkadam.foodiesapi.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final OrderRepository orderRepository;

    public DashboardStatsResponse getStats() {
        LocalDateTime now          = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay();

        // ── User stats ───────────────────────────────────────────────────────
        long totalUsers   = userRepository.count();
        long activeUsers  = userRepository.countByAccountStatus("ACTIVE");
        long blockedUsers = userRepository.countByAccountStatus("BLOCKED");
        long newToday     = userRepository.countByCreatedAtAfter(startOfToday);
        long newThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);

        // ── Food stats ───────────────────────────────────────────────────────
        long totalFoods      = foodRepository.count();
        long totalCategories = foodRepository.findAll().stream()
                .map(f -> f.getCategory()).filter(Objects::nonNull).distinct().count();

        // ── Order stats ──────────────────────────────────────────────────────
        List<OrderEntity> allOrders = orderRepository.findAll();
        long totalOrders          = allOrders.size();
        long pendingOrders        = allOrders.stream().filter(o -> "preparing".equals(o.getOrderStatus())).count();
        long deliveredOrders      = allOrders.stream().filter(o -> "delivered".equals(o.getOrderStatus())).count();
        long cancelledOrders      = allOrders.stream().filter(o -> "cancelled".equals(o.getOrderStatus())).count();
        long outForDeliveryOrders = allOrders.stream().filter(o -> "out for delivery".equals(o.getOrderStatus())).count();

        // ── Revenue ──────────────────────────────────────────────────────────
        double totalRevenue = allOrders.stream()
                .filter(o -> "paid".equals(o.getPaymentStatus()))
                .mapToDouble(OrderEntity::getAmount).sum();

        double monthlyRevenue = allOrders.stream()
                .filter(o -> "paid".equals(o.getPaymentStatus())
                        && o.getCreatedAt() != null
                        && o.getCreatedAt().isAfter(startOfMonth))
                .mapToDouble(OrderEntity::getAmount).sum();

        double todayRevenue = allOrders.stream()
                .filter(o -> "paid".equals(o.getPaymentStatus())
                        && o.getCreatedAt() != null
                        && o.getCreatedAt().isAfter(startOfToday))
                .mapToDouble(OrderEntity::getAmount).sum();

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .blockedUsers(blockedUsers)
                .newUsersToday(newToday)
                .newUsersThisMonth(newThisMonth)
                .totalFoodItems(totalFoods)
                .totalCategories(totalCategories)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .outForDeliveryOrders(outForDeliveryOrders)
                .totalRevenue(totalRevenue)
                .monthlyRevenue(monthlyRevenue)
                .todayRevenue(todayRevenue)
                .monthlyRevenueChart(buildMonthlyRevenueChart(allOrders))
                .monthlyOrdersChart(buildMonthlyOrdersChart(allOrders))
                .monthlyUsersChart(buildMonthlyUsersChart())
                .topSellingFoods(buildTopSellingFoods(allOrders))
                .topCategories(buildTopCategories(allOrders))
                .topCustomers(buildTopCustomers(allOrders))
                .build();
    }

    private List<Map<String, Object>> buildMonthlyRevenueChart(List<OrderEntity> orders) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");
        Map<String, Double> map = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) map.put(LocalDate.now().minusMonths(i).format(fmt), 0.0);
        orders.stream()
                .filter(o -> "paid".equals(o.getPaymentStatus()) && o.getCreatedAt() != null)
                .forEach(o -> map.computeIfPresent(o.getCreatedAt().format(fmt), (k, v) -> v + o.getAmount()));
        return map.entrySet().stream()
                .map(e -> Map.<String, Object>of("month", e.getKey(), "revenue", e.getValue()))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildMonthlyOrdersChart(List<OrderEntity> orders) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");
        Map<String, Long> map = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) map.put(LocalDate.now().minusMonths(i).format(fmt), 0L);
        orders.stream()
                .filter(o -> o.getCreatedAt() != null)
                .forEach(o -> map.computeIfPresent(o.getCreatedAt().format(fmt), (k, v) -> v + 1));
        return map.entrySet().stream()
                .map(e -> Map.<String, Object>of("month", e.getKey(), "orders", e.getValue()))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildMonthlyUsersChart() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");
        Map<String, Long> map = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) map.put(LocalDate.now().minusMonths(i).format(fmt), 0L);
        userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null)
                .forEach(u -> map.computeIfPresent(u.getCreatedAt().format(fmt), (k, v) -> v + 1));
        return map.entrySet().stream()
                .map(e -> Map.<String, Object>of("month", e.getKey(), "users", e.getValue()))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildTopSellingFoods(List<OrderEntity> orders) {
        Map<String, long[]> sales = new HashMap<>();
        orders.forEach(o -> {
            if (o.getOrderedItems() == null) return;
            o.getOrderedItems().forEach(item -> {
                String name = item.getName() != null ? item.getName() : "Unknown";
                sales.computeIfAbsent(name, k -> new long[]{0, 0});
                int qty = item.getQuantity() != null ? item.getQuantity() : 1;
                sales.get(name)[0] += qty;
                sales.get(name)[1] += (long) item.getPrice();
            });
        });
        return sales.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]))
                .limit(5)
                .map(e -> Map.<String, Object>of("name", e.getKey(), "qty", e.getValue()[0], "revenue", e.getValue()[1]))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildTopCategories(List<OrderEntity> orders) {
        Map<String, Long> catCount = new HashMap<>();
        orders.forEach(o -> {
            if (o.getOrderedItems() == null) return;
            o.getOrderedItems().forEach(item -> {
                String cat = item.getCategory() != null ? item.getCategory() : "Other";
                int qty = item.getQuantity() != null ? item.getQuantity() : 1;
                catCount.merge(cat, (long) qty, Long::sum);
            });
        });
        return catCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(6)
                .map(e -> Map.<String, Object>of("category", e.getKey(), "orders", e.getValue()))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildTopCustomers(List<OrderEntity> orders) {
        Map<String, double[]> data = new HashMap<>();
        orders.stream()
                .filter(o -> "paid".equals(o.getPaymentStatus()) && o.getEmail() != null)
                .forEach(o -> {
                    data.computeIfAbsent(o.getEmail(), k -> new double[]{0, 0});
                    data.get(o.getEmail())[0] += o.getAmount();
                    data.get(o.getEmail())[1] += 1;
                });
        return data.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue()[0], a.getValue()[0]))
                .limit(5)
                .map(e -> Map.<String, Object>of("email", e.getKey(), "totalSpent", e.getValue()[0], "orderCount", (long) e.getValue()[1]))
                .collect(Collectors.toList());
    }
}
