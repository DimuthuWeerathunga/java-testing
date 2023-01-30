package com.amigoscode.testing.payment;

import java.math.BigDecimal;
import java.util.UUID;

public class Payment {

    private Long paymentId;

    private UUID customerId;

    private BigDecimal amount;

    private Currency currency;

    private String source;

    private String description;
}
