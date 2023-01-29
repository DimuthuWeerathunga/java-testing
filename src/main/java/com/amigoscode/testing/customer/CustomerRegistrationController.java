package com.amigoscode.testing.customer;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/customer-registration")
public class CustomerRegistrationController {

    public void registerNewCustomer(CustomerRegistrationRequest request){

    }

}
