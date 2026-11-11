package ek.osnb.demo.coffeeshop.customer;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
class CustomerController {

    private final CustomerService service;

    CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping("/me")
    CustomerResponse showCustomer(Authentication auth) {
        return service.showCustomer(auth.getName());
    }

    @PutMapping("/me")
    CustomerResponse saveCustomer(Authentication auth,
                                  @Valid @RequestBody ProfileRequest request) {
        return service.saveCustomer(auth.getName(), request);
    }
}
