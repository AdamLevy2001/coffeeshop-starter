package ek.osnb.demo.coffeeshop.order;

import ek.osnb.demo.coffeeshop.cart.CartApi;
import ek.osnb.demo.coffeeshop.cart.CartInfo;
import ek.osnb.demo.coffeeshop.cart.CartLineInfo;
import ek.osnb.demo.coffeeshop.catalog.CatalogApi;
import ek.osnb.demo.coffeeshop.catalog.ProductInfo;
import ek.osnb.demo.coffeeshop.common.ConflictException;
import ek.osnb.demo.coffeeshop.common.NotFoundException;
import ek.osnb.demo.coffeeshop.customer.CustomerApi;
import ek.osnb.demo.coffeeshop.customer.CustomerInfo;
import org.aspectj.weaver.ast.Or;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Orders use three other features, always through their public *Api interface:
 * CustomerApi (who is ordering), CartApi (what is ordered) and CatalogApi (names,
 * prices and stock). The order itself only stores their ids, plus copies of the
 * values that must never change afterwards.
 *
 * Serves two kinds of callers:
 * - OrderController (the REST API), with *Response records
 * - other features, through the public OrderApi interface, with *Info records
 */
@Service
@Transactional
class OrderService implements OrderApi {

    private final OrderRepository repository;
    private final CustomerApi customers;
    private final CartApi carts;
    private final CatalogApi catalog;
    private final ApplicationEventPublisher events;

    OrderService(
            OrderRepository repository,
            CustomerApi customers,
            CartApi carts,
            CatalogApi catalog,
            ApplicationEventPublisher events) {

        this.repository = repository;
        this.customers = customers;
        this.carts = carts;
        this.catalog = catalog;
        this.events = events;
    }

    // Everything in here happens in one transaction: if any step fails (e.g. one
    // product is out of stock), nothing is saved - no stock is taken, no order is
    // created and the cart keeps its items.
    OrderResponse placeOrder(String email, PlaceOrderRequest request) {
        CustomerInfo customer = findProfile(email);
        CartInfo cart = carts.getCart(request.cartId());

        if (cart.isEmpty()) {
            throw new ConflictException("The cart is empty");
        }

        List<Long> productIds = new ArrayList<>();
        for (CartLineInfo line : cart.lines()) {
            productIds.add(line.productId());
        }
        Map<Long, ProductInfo> products = catalog.findProducts(productIds);

        List<OrderLine> lines = new ArrayList<>();

        for (CartLineInfo cartLine : cart.lines()) {
            ProductInfo product = products.get(cartLine.productId());

            if (product == null) {
                throw new NotFoundException("Product not found: " + cartLine.productId());
            }

            // Throws InsufficientStockException (409) if there are too few left
            catalog.reserveStock(product.id(), cartLine.quantity());

            // Copy the name and price: the order keeps them even if the catalog changes
            lines.add(new OrderLine(product.id(), product.name(), product.price(), cartLine.quantity()));
        }

        ShippingAddress address = new ShippingAddress(
                customer.name(), customer.street(), customer.postalCode(), customer.city());

        Order order = repository.save(Order.place(customer.id(), address, lines, Instant.now()));

        carts.clearCart(cart.id());

        // Lets other features react, without the order feature knowing about them
        events.publishEvent(new OrderPlacedEvent(
                order.getId(), order.getCustomerId(), order.getTotal(), order.getPlacedAt()));

        return OrderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    List<OrderSummaryResponse> findByCustomer(String email) {
        Optional<CustomerInfo> existing = customers.findCustomerByEmail(email);

        if (existing.isEmpty()) {
            return List.of();
        }

        List<OrderSummaryResponse> summaries = new ArrayList<>();

        for (Order order : repository.findByCustomerIdOrderByPlacedAtDesc(existing.get().id())) {
            summaries.add(OrderMapper.toSummary(order));
        }

        return summaries;
    }

    @Transactional(readOnly = true)
    OrderResponse findById(String email, Long id) {
        return OrderMapper.toResponse(findOwnOrder(email, id));
    }

    OrderResponse cancel(String email, Long id) {
        Order order = findOwnOrder(email, id);
        order.cancel();

        // The items go back on the shelf
        for (OrderLine line : order.getLines()) {
            catalog.releaseStock(line.getProductId(), line.getQuantity());
        }

        return OrderMapper.toResponse(order);
    }

    OrderResponse ship(Long id) {
        Order order = findOrder(id);
        order.ship();

        return OrderMapper.toResponse(order);
    }

    // ---------- OrderApi: used by other features ----------

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryInfo> findOrders(Long customerId) {
        List<OrderSummaryInfo> result = new ArrayList<>();

        for (Order order : repository.findByCustomerIdOrderByPlacedAtDesc(customerId)) {
            result.add(OrderMapper.toSummaryInfo(order));
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderInfo getOrder(Long customerId, Long orderId) {
        Optional<Order> result = repository.findByIdAndCustomerId(orderId, customerId);

        // The same answer for someone else's order as for a missing one,
        // so a caller can't find out which order ids exist
        if (result.isEmpty()) {
            throw new NotFoundException("Order not found: " + orderId);
        }

        return OrderMapper.toInfo(result.get());
    }

    private Order findOrder(Long id) {
        Optional<Order> result = repository.findById(id);

        if (result.isEmpty()) {
            throw new NotFoundException("Order not found: " + id);
        }

        return result.get();
    }

    private CustomerInfo findProfile(String email) {
        CustomerInfo customerInfo = customers.findCustomerByEmail(email)
                .orElseThrow(() -> new ConflictException("Please fill in your delivery details first"));
        return customerInfo;
    }

    private Order findOwnOrder(String email, Long id) {
        CustomerInfo customerInfo = customers.findCustomerByEmail(email)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        Order order = repository.findByIdAndCustomerId(id, customerInfo.id())
                .orElseThrow(() -> new NotFoundException("Order not found"));
        return order;
    }
}
