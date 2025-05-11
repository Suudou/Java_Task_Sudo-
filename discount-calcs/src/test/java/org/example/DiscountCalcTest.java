package org.example;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DiscountCalcTest {

    @Test
    public void testFullPaymentWithPoints() {
        DiscountCalc.Order order = new DiscountCalc.Order();
        order.id = "ORDER1";
        order.value = 100.0;
        order.promotions = new ArrayList<>();

        DiscountCalc.PaymentMethod points = new DiscountCalc.PaymentMethod();
        points.id = "PUNKTY";
        points.discount = 15.0;
        points.limit = 100.0;

        List<DiscountCalc.Order> orders = List.of(order);
        List<DiscountCalc.PaymentMethod> methods = List.of(points);

        Map<String, Double> result = DiscountCalc.computePayments(orders, methods);

        assertEquals(85.0, result.get("PUNKTY"), 0.01); // 100 - 15% = 85
    }

    @Test
    public void testPaymentWithCardPromotion() {
        DiscountCalc.Order order = new DiscountCalc.Order();
        order.id = "ORDER2";
        order.value = 200.0;
        order.promotions = List.of("mZysk");

        DiscountCalc.PaymentMethod mZysk = new DiscountCalc.PaymentMethod();
        mZysk.id = "mZysk";
        mZysk.discount = 10.0;
        mZysk.limit = 200.0;

        List<DiscountCalc.Order> orders = List.of(order);
        List<DiscountCalc.PaymentMethod> methods = List.of(mZysk);

        Map<String, Double> result = DiscountCalc.computePayments(orders, methods);

        assertEquals(180.0, result.get("mZysk"), 0.01); // 200 - 10% = 180
    }

    @Test
    public void testFullPaymentWithPointsWhenMoreProfitable() {
        DiscountCalc.Order order = new DiscountCalc.Order();
        order.id = "ORDER3";
        order.value = 50.0;
        order.promotions = List.of("mZysk");

        DiscountCalc.PaymentMethod points = new DiscountCalc.PaymentMethod();
        points.id = "PUNKTY";
        points.discount = 15.0; // Większy rabat punktami
        points.limit = 100.0; // Wystarczający limit

        DiscountCalc.PaymentMethod mZysk = new DiscountCalc.PaymentMethod();
        mZysk.id = "mZysk";
        mZysk.discount = 5.0; // Mniejszy rabat kartą
        mZysk.limit = 50.0;

        List<DiscountCalc.Order> orders = List.of(order);
        List<DiscountCalc.PaymentMethod> methods = Arrays.asList(points, mZysk);

        Map<String, Double> result = DiscountCalc.computePayments(orders, methods);

        assertEquals(42.5, result.get("PUNKTY"), 0.01); // 50 - 15% = 42.5
        assertEquals(0.0, result.get("mZysk"), 0.01); // Karta nie jest używana
    }
}