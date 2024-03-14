package com.amigoscode.testing.customer;

import com.amigoscode.testing.utils.PhoneNumberValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

//@ExtendWith(MockitoExtension.class)
class CustomerRegistrationServiceTest {

    @Mock
    private CustomerRepository customerRepository;// = mock(CustomerRepository.class);
    @Mock
    private PhoneNumberValidator phoneNumberValidator;
    @Captor
    private ArgumentCaptor<Customer> customerArgumentCaptor;

    private CustomerRegistrationService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new CustomerRegistrationService(customerRepository, phoneNumberValidator);
    }

    @Test
    void itShouldSaveNewCustomer() {
        // Given a phone number and a customer
        String phoneNumber = "000099";
        Customer customer = new Customer(UUID.randomUUID(), "Mariyam", phoneNumber);

        // a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // no existing customer with phone number passed
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.empty());

        // ... Valid phone number
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        // When
        underTest.registerNewCustomer(request);

        // Then
        then(customerRepository).should().save(customerArgumentCaptor.capture());
        Customer customerArgumentCaptorValue = customerArgumentCaptor.getValue();
        assertThat(customerArgumentCaptorValue).isEqualToComparingFieldByField(customer);
    }

    @Test
    void itShouldNotSaveNewCustomerWhenPhoneNumberIsInvalid() {
        // Given a phone number and a customer
        String phoneNumber = "000099";
        Customer customer = new Customer(UUID.randomUUID(), "Mariyam", phoneNumber);

        // a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... Invalid phone number
        given(phoneNumberValidator.test(phoneNumber)).willReturn(false);

        // When
        // Then
        assertThatThrownBy(() -> underTest.registerNewCustomer(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Phone number " + phoneNumber + " is not valid");
        then(customerRepository).shouldHaveNoInteractions();
    }

    @Test
    void itShouldNotSaveCustomerWhenCustomerExists() {
        // Given a phone number and a customer
        String phoneNumber = "000099";
        Customer customer = new Customer(UUID.randomUUID(), "Mariyam", phoneNumber);

        // a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // an existing customer is returned
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.of(customer));

        // ... Valid phone number
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        // When
        underTest.registerNewCustomer(request);

        // Then
        then(customerRepository).should(never()).save(any());
    }

    @Test
    void itShouldSaveNewCustomerWhenIdIsNull() {
        // Given a phone number and a customer
        String phoneNumber = "000099";
        Customer customer = new Customer(null, "Mariyam", phoneNumber);

        // a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // no customer with phone number passed
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.empty());

        // ... Valid phone number
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        // When
        underTest.registerNewCustomer(request);

        // Then
        then(customerRepository).should().save(customerArgumentCaptor.capture());
        Customer customerArgumentCaptorValue = customerArgumentCaptor.getValue();
        assertThat(customerArgumentCaptorValue)
                .isEqualToIgnoringGivenFields(customer,"id");
        assertThat(customerArgumentCaptorValue.getId()).isNotNull();
    }

    @Test
    void itShouldThrowWhenPhoneNumberIsTaken() {
        // Given a phone number and a customer
        String phoneNumber = "000099";
        Customer customer = new Customer(UUID.randomUUID(), "Mariyam", phoneNumber);
        Customer customer2 = new Customer(UUID.randomUUID(), "John", phoneNumber);

        // a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // an existing customer is returned
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.of(customer2));

        // ... Valid phone number
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        // When
        // Then
        assertThatThrownBy(()-> underTest.registerNewCustomer(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format("phone number [%s] is taken", phoneNumber));

        // Finally
        then(customerRepository).should(never()).save(any(Customer.class));
    }
}