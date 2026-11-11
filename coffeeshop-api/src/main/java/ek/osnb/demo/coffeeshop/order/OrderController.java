package ek.osnb.demo.coffeeshop.order;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
class OrderController {

    private final OrderService service;

    OrderController(OrderService service) {
        this.service = service;
    }

    // Checkout: turns a cart into an order
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OrderResponse placeOrder(@Valid @RequestBody PlaceOrderRequest request, Authentication auth) {
        return service.placeOrder(auth.getName(), request);
    }


    @GetMapping
    List<OrderSummaryResponse> findByCustomer (Authentication auth) {
        return service.findByCustomer(auth.getName());
    }

    @GetMapping("/{id}")
    OrderResponse findById(@PathVariable Long id, Authentication auth) {
        return service.findById(auth.getName(), id);
    }

    @PostMapping("/{id}/cancel")
    OrderResponse cancel(@PathVariable Long id, Authentication auth) {
        return service.cancel(auth.getName(), id);
    }
    //Kun staff, se SecurityConfig
    @PostMapping("/{id}/ship")
    OrderResponse ship(@PathVariable Long id) {
        return service.ship(id);
    }
}
