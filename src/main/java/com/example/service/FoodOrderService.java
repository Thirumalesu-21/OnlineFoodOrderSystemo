package com.example.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.Exception.OrderNotFoundException;
import com.example.model.FoodOrder;

@Service
public class FoodOrderService {

    private final List<FoodOrder> orders = new ArrayList<>();
    private long nextId = 1;

    // GET: Retrieve all orders
    public List<FoodOrder> getAllOrders() {
        return new ArrayList<>(orders);
    }

    // GET: Retrieve an order by ID
    public FoodOrder getOrderById(Long id) {
        return orders.stream()
                .filter(order -> order.getOrderId()==(id))
                .findFirst()
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with ID: " + id));
    }

    // POST: Create a new order
    public FoodOrder addOrder(FoodOrder order) {
        order.setOrderId(nextId++);

        FoodOrder savedOrder = new FoodOrder(
                order.getOrderId(),
                order.getCustomerName(),
                order.getFoodItem(),
                order.getQuantity(),
                order.getPrice()
        );

        orders.add(savedOrder);
        return savedOrder;
    }

    // PUT: Replace all editable fields of an existing order
    public FoodOrder updateOrder(Long id, FoodOrder request) {
        FoodOrder existing = getOrderById(id);

        existing.setCustomerName(request.getCustomerName());
        existing.setFoodItem(request.getFoodItem());
        existing.setQuantity(request.getQuantity());
        existing.setPrice(request.getPrice());

        return existing;
    }

    // DELETE: Delete an order
    public String deleteOrder(Long id) {
        FoodOrder existing = getOrderById(id);
        orders.remove(existing);

        return "Order deleted successfully. ID: " + id;
    }

    // PATCH: Update only the fields supplied by the client
    public FoodOrder patchOrder(Long id, Map<String, Object> updates) {
        FoodOrder existing = getOrderById(id);

        if (updates == null || updates.isEmpty()) {
            throw new IllegalArgumentException(
                    "Provide at least one field to update");
        }

        for (String field : updates.keySet()) {
            switch (field) {
                case "customerName":
                    Object name = updates.get(field);
                    if (!(name instanceof String)
                            || ((String) name).isBlank()) {
                        throw new IllegalArgumentException(
                                "Customer name is required");
                    }
                    existing.setCustomerName((String) name);
                    break;

                case "foodItem":
                    Object food = updates.get(field);
                    if (!(food instanceof String)
                            || ((String) food).isBlank()) {
                        throw new IllegalArgumentException(
                                "Food item cannot be empty");
                    }
                    existing.setFoodItem((String) food);
                    break;

                case "quantity":
                    Object quantity = updates.get(field);
                    if (!(quantity instanceof Number)
                            || ((Number) quantity).intValue() < 1
                            || ((Number) quantity).doubleValue()
                               != ((Number) quantity).intValue()) {
                        throw new IllegalArgumentException(
                                "Quantity must be a positive whole number");
                    }
                    existing.setQuantity(((Number) quantity).intValue());
                    break;

                case "price":
                    Object price = updates.get(field);
                    BigDecimal amount;
                    try {
                        if (!(price instanceof Number)
                                && !(price instanceof String)) {
                            throw new NumberFormatException();
                        }
                        amount = new BigDecimal(price.toString());
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException(
                                "Price must be a valid number");
                    }

                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException(
                                "Price must be greater than 0");
                    }
                    existing.setPrice(amount);
                    break;

                case "orderId":
                    throw new IllegalArgumentException(
                            "Order ID cannot be updated");

                default:
                    throw new IllegalArgumentException(
                            "Unknown field: " + field);
            }
        }

        return existing;
    }
}
