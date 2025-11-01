package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.PaymentTransaction;
import com.officefood.healthy_food_api.model.Order;
import com.officefood.healthy_food_api.model.enums.PaymentMethod;
import java.util.*;

public interface PaymentService {
    PaymentTransaction authorize(Order order, PaymentMethod method, double amount);
    PaymentTransaction capture(Order order, String paymentId);
    PaymentTransaction refund(Order order, String paymentId, double amount);
}
