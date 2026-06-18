package com.ticketstop.payment.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketstop.payment.domain.Payment;
import com.ticketstop.payment.dto.CreatePaymentRequest;
import com.ticketstop.payment.dto.PaymentResponse;
import com.ticketstop.payment.dto.PaymentWebhookRequest;
import com.ticketstop.payment.repository.PaymentRepository;

@Service
public class PaymentService {
    private final PaymentRepository payments;

    public PaymentService(PaymentRepository payments) {
        this.payments = payments;
    }

    @Transactional
    public PaymentResponse create(CreatePaymentRequest request) {
        return payments.findByOrderId(request.orderId())
            .map(PaymentResponse::from)
            .orElseGet(() -> PaymentResponse.from(payments.save(new Payment(
                UUID.randomUUID(),
                request.orderId(),
                request.provider(),
                request.amount(),
                request.currency().toUpperCase()
            ))));
    }

    @Transactional
    public PaymentResponse webhook(PaymentWebhookRequest request) {
        Payment payment = payments.findByOrderId(request.orderId())
            .orElseThrow(() -> new IllegalArgumentException("Payment not found for order"));
        payment.applyProviderResult(request.providerReference(), request.status(), request.failureReason());
        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse get(UUID id) {
        return payments.findById(id).map(PaymentResponse::from)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
    }
}
