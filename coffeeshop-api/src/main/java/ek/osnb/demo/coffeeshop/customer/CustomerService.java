package ek.osnb.demo.coffeeshop.customer;

import ek.osnb.demo.coffeeshop.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
class CustomerService implements CustomerApi {

    private final CustomerRepository repository;

    CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    // ---------- Used by CustomerController ----------

    //I den her metode henter jeg profilen for den kunde som er logget ind
    //Hvis den ikke kan finde profilen, kaster jeg en ikke fundet exception, hvilket er 404 som passer med frontenden
    @Transactional(readOnly = true)
    CustomerResponse showCustomer(String email) {
        Customer customer = repository.findByEmail(Customer.normalizeEmail(email))
                .orElseThrow(() -> new NotFoundException("Customer profile not found"));
        return CustomerMapper.toResponse(customer);
    }

    //I den her metode opretter jeg en ny kunde med en profil eller opdaterer med en eksisterende profil, afhængigt om profilen findes
    //Emailen kommer fra login og ikke fra request, fordi at en kunde ikke skal ændre en andens profil
    CustomerResponse saveCustomer(String email, ProfileRequest request) {
        Optional<Customer> existing = repository.findByEmail(Customer.normalizeEmail(email));

        if (existing.isPresent()) {
            Customer customer = existing.get();
            customer.updateDetails(request.name(),
                    email,
                    request.street(),
                    request.postalCode(),
                    request.city());
            return CustomerMapper.toResponse(customer);
        }

        Customer customer = Customer.create(request.name(),
                email,
                request.street(),
                request.postalCode(),
                request.city());
        return CustomerMapper.toResponse(repository.save(customer));
    }



// ---------- CustomerApi: used by other features ----------

    @Override
    @Transactional(readOnly = true)
    public CustomerInfo getCustomer(Long customerId) {
        return CustomerMapper.toInfo(findCustomer(customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomerInfo> findCustomerByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }

        return repository.findByEmail(Customer.normalizeEmail(email))
                .map(CustomerMapper::toInfo);
    }

    private Customer findCustomer(Long id) {
        Optional<Customer> result = repository.findById(id);

        if (result.isEmpty()) {
            throw new NotFoundException("Customer not found: " + id);
        }

        return result.get();
    }
}
