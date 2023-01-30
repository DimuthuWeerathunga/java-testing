package com.amigoscode.testing.payment;

import java.math.BigDecimal;

public interface CardPaymentCharger {

    CardPaymentCHarge chargeCard(
            String cardSource,
            BigDecimal amount,
            Currency currency,
            String description
    );
}
