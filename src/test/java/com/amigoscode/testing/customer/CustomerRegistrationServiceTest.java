package com.amigoscode.testing.customer;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CustomerRegistrationServiceTest {

//    @Mock
    private CustomerRepository customerRepository = mock(CustomerRepository.class);

    private CustomerRegistrationService underTest;

    @BeforeEach
    void setUp() {
//        MockitoAnnotations.initMocks(this);
        underTest = new CustomerRegistrationService();
    }
}