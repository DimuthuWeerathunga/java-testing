package com.amigoscode.testing.payment;

import com.amigoscode.testing.customer.Customer;
import com.amigoscode.testing.customer.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

class PaymentServiceTest {
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private CardPaymentCharger cardPaymentCharger;

    private PaymentService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new PaymentService(customerRepository, paymentRepository, cardPaymentCharger);
    }

    @Test
    void itShouldChargeCardSuccessfully() {
        // Given
        UUID customerId = UUID.randomUUID();

        // ... Customer exists
        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));

        // ... Payment request
        PaymentRequest paymentRequest = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        new BigDecimal("100.00"),
                        Currency.USD,
                        "card123xx",
                        "Donation"
                )
        );

        // ... Card is charged successfully
        given(cardPaymentCharger.chargeCard(
                paymentRequest.getPayment().getSource(),
                paymentRequest.getPayment().getAmount(),
                paymentRequest.getPayment().getCurrency(),
                paymentRequest.getPayment().getDescription()
        )).willReturn(new CardPaymentCharge(true));

        // When
        underTest.chargeCard(customerId, paymentRequest);

        // Then
        ArgumentCaptor<Payment> paymentArgumentCaptor
                = ArgumentCaptor.forClass(Payment.class);

        then(paymentRepository).should().save(paymentArgumentCaptor.capture());

        Payment paymentArgumentCaptorValue = paymentArgumentCaptor.getValue();
        assertThat(paymentArgumentCaptorValue)
                .isEqualToIgnoringGivenFields(
                        paymentRequest.getPayment(),
                        "customerId"
                );

        assertThat(paymentArgumentCaptorValue.getCustomerId()).isEqualTo(customerId);
    }

    @Test
    void itShouldThrowWhenCardIsNotCharged() {
        // Given
        UUID customerId = UUID.randomUUID();

        // ... Customer exists
        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));

        // ... Payment request
        PaymentRequest paymentRequest = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        new BigDecimal("100.00"),
                        Currency.USD,
                        "card123xx",
                        "Donation"
                )
        );

        // ... Card is not charged successfully
        given(cardPaymentCharger.chargeCard(
                paymentRequest.getPayment().getSource(),
                paymentRequest.getPayment().getAmount(),
                paymentRequest.getPayment().getCurrency(),
                paymentRequest.getPayment().getDescription()
        )).willReturn(new CardPaymentCharge(false));

        // When
        // Then
        assertThatThrownBy(() -> underTest.chargeCard(customerId, paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format("Card not debited for customer %s", customerId));
        then(paymentRepository).should(never()).save(any(Payment.class));
    }

    @Test
    void itShouldNotChargeAndThrowWhenCurrencyNotSupported() {
        // Given
        UUID customerId = UUID.randomUUID();

        // ... Customer exists
        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));

        // ... Payment request
        PaymentRequest paymentRequest = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        new BigDecimal("100.00"),
                        Currency.EUR,
                        "card123xx",
                        "Donation"
                )
        );
        // When
        assertThatThrownBy(() -> underTest.chargeCard(customerId, paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        String.format("Currency [%s] not supported", paymentRequest.getPayment().getCurrency())
                );


        // Then
        // ... No interaction with cardPaymentCharger
        then(cardPaymentCharger).shouldHaveNoInteractions();

        // Payment should not be saved
        then(paymentRepository).shouldHaveNoInteractions();
    }

    @Test
    void itShouldNotChargeAndThrowWhenCustomerNotFound() {
        // Given
        UUID customerId = UUID.randomUUID();

        // Customer not found in db
        given(customerRepository.findById(customerId)).willReturn(Optional.empty());

        // When
        // Then
        assertThatThrownBy(() -> underTest.chargeCard(customerId, new PaymentRequest(new Payment())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format("Customer with [%s] id not found", customerId));
    }
}