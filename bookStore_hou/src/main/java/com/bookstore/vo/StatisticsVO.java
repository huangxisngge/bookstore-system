package com.bookstore.vo;

import lombok.Data;

@Data
public class StatisticsVO {

    @Data
    public static class BookSalesRanking {
        private String bookName;
        private Long totalSales;
    }

    @Data
    public static class MonthlySales {
        private String month;
        private Double totalAmount;
        private Long orderCount;
    }

    @Data
    public static class CategorySales {
        private String categoryName;
        private Double totalSales;
        private Long orderCount;
    }

    @Data
    public static class InventoryAlert {
        private Long id;
        private String title;
        private String author;
        private Integer stock;
    }

    @Data
    public static class OverviewStats {
        private Long totalUsers;
        private Long totalBooks;
        private Long totalOrders;
        private Double totalRevenue;
        private Long pendingOrders;
        private Long lowStockBooks;
        private Long returnOrders;
        private Double returnAmount;
    }

    @Data
    public static class ReturnTrend {
        private String month;
        private Long returnCount;
        private Double returnAmount;
    }
}
