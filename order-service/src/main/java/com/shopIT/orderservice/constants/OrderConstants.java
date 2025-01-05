package com.shopit.orderservice.constants;

public class OrderConstants {
    public static final String API_CHECK = "%s Token available, API call to %s !";
    public static final String PLACED_ORDER = "Placed order with ID: ";
    public static final String ORDER_404 = "Order with this ID not present !!";
    public static final String INVENTORY_UNREACHABLE = "Can't reach Inventory Server, Try again later !!";
    public static final String INVENTORY_REACHABLE = "Successfully called inventory server from order service !";
    public static final String PRODUCT_NOT_IN_STOCK = "Product(s) is out of stock !!";
    public static final String ORDER_NOT_SAVED = "Error while saving/finalizing the order !!";
    public static final String RATE_LIMIT_EXCEEDED = "Rate Limit Exceeded !!";
}