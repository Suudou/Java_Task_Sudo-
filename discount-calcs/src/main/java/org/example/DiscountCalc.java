package org.example;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.*;

public class DiscountCalc {
    // Constants defining the loyalty points identifier and discount thresholds
    private static final String POINTS_ID = "PUNKTY";
    private static final double MIN_POINTS_PERCENTAGE = 0.10;
    private static final double PARTIAL_POINTS_DISCOUNT = 10.0;

    // Class representing an order
    static class Order {
        public String id;
        public double value;
        public List<String> promotions = new ArrayList<>();

        public Order() {}
    }

    // Class representing a payment method
    static class PaymentMethod {
        public String id;
        public double discount;
        public double limit;

        public PaymentMethod() {}
    }

    public static void main(String[] args) {
        // Check the validity of the number of arguments (paths to JSON files)
        if (args == null || args.length == 0) {
            System.err.println("Error: No arguments provided.");
            printUsage();
            System.exit(1);
        }

        if (args.length == 1) {
            System.err.println("Error: Only one argument provided: " + args[0]);
            printUsage();
            System.exit(1);
        }

        if (args.length != 2) {
            System.err.println("Error: Expected exactly two arguments, but received: " + args.length);
            System.err.println("Provided arguments: " + Arrays.toString(args));
            printUsage();
            System.exit(1);
        }

        // Retrieve file paths from arguments
        String ordersPath = args[0];
        String paymentMethodsPath = args[1];

        // Initialize ObjectMapper for JSON parsing
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Load orders from file
            System.out.println("Loading orders from: " + ordersPath);
            List<Order> orders = mapper.readValue(new File(ordersPath),
                    mapper.getTypeFactory().constructCollectionType(List.class, Order.class));
            System.out.println("Loaded " + orders.size() + " orders.");

            // Load payment methods from file
            System.out.println("Loading payment methods from: " + paymentMethodsPath);
            List<PaymentMethod> methods = mapper.readValue(new File(paymentMethodsPath),
                    mapper.getTypeFactory().constructCollectionType(List.class, PaymentMethod.class));
            System.out.println("Loaded " + methods.size() + " payment methods.");

            Map<String, Double> spent = computePayments(orders, methods);

            // Print the results
            for (Map.Entry<String, Double> entry : spent.entrySet()) {
                if (entry.getValue() > 1e-10) {
                    System.out.printf("%s %.2f%n", entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            // Handle errors during loading or computation
            System.err.println("Error: " + e.getMessage());
            System.err.println("Details: " + e.getClass().getName());
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Method to display program usage instructions
    private static void printUsage() {
        System.err.println("Usage: java DiscountCalc <path_to_orders.json> <path_to_paymentmethods.json>");
        System.err.println("Example: java DiscountCalc /home/user/orders.json /home/user/paymentmethods.json");
    }

    // Method to compute optimal payments for orders
    protected static Map<String, Double> computePayments(List<Order> orders, List<PaymentMethod> methods) {
        // Map of available limits for each payment method
        Map<String, Double> limits = new HashMap<>();
        // Map mapping payment method IDs to PaymentMethod objects
        Map<String, PaymentMethod> methodMap = new HashMap<>();
        for (PaymentMethod m : methods) {
            limits.put(m.id, m.limit); // Initialize limits
            methodMap.put(m.id, m);
        }

        // Map to store spent amounts for each method
        Map<String, Double> spent = new HashMap<>();
        methods.forEach(m -> spent.put(m.id, 0.0)); // Initialize with zero values

        for (Order order : orders) {
            double value = order.value; // Order value
            double bestDiscount = -1.0; // Best discount found
            Map<String, Double> bestPayments = null; // Best payment combination

            // Check for full payment with loyalty points
            PaymentMethod points = methodMap.get(POINTS_ID);
            if (points != null && limits.getOrDefault(POINTS_ID, 0.0) >= value * (1 - points.discount / 100)) {
                double discount = value * points.discount / 100; // Calculate discount
                if (discount > bestDiscount) {
                    bestDiscount = discount;
                    bestPayments = Map.of(POINTS_ID, value - discount); // Pay with points
                }
            }

            // Check for payment with a promotional card
            for (String promo : order.promotions) {
                PaymentMethod method = methodMap.get(promo);
                if (method != null && !method.id.equals(POINTS_ID) && limits.getOrDefault(method.id, 0.0) >= value * (1 - method.discount / 100)) {
                    double discount = value * method.discount / 100;
                    if (discount > bestDiscount) {
                        bestDiscount = discount;
                        bestPayments = Map.of(method.id, value - discount); // Pay with card
                    }
                }
            }

            // Check for partial payment with points and card
            if (points != null && limits.getOrDefault(POINTS_ID, 0.0) >= value * MIN_POINTS_PERCENTAGE) {
                for (PaymentMethod method : methods) {
                    if (method.id.equals(POINTS_ID)) continue; // Skip points
                    double discount = value * PARTIAL_POINTS_DISCOUNT / 100; // 10% discount
                    double cost = value - discount; // Cost after discount
                    double pointsUsed = Math.max(value * MIN_POINTS_PERCENTAGE, Math.min(limits.getOrDefault(POINTS_ID, 0.0), cost)); // Points usage
                    double cardUsed = cost - pointsUsed; // Card usage
                    if (cardUsed >= 0 && limits.getOrDefault(method.id, 0.0) >= cardUsed && discount > bestDiscount) {
                        bestDiscount = discount;
                        bestPayments = new HashMap<>(); // New payment combination
                        if (pointsUsed > 0) bestPayments.put(POINTS_ID, pointsUsed);
                        if (cardUsed > 0) bestPayments.put(method.id, cardUsed);
                    }
                }
            }

            // Apply the best payment combination
            if (bestPayments != null) {
                for (var entry : bestPayments.entrySet()) {
                    String methodId = entry.getKey();
                    double amount = entry.getValue();
                    spent.merge(methodId, amount, Double::sum);
                    limits.merge(methodId, -amount, Double::sum);
                }
            } else {
                System.err.println("Cannot pay for order: " + order.id);
            }
        }

        return spent;
    }
}