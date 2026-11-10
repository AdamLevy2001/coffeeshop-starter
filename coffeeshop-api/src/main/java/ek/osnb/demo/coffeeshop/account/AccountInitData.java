package ek.osnb.demo.coffeeshop.account;

import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class AccountInitData implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    AccountInitData (AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository=accountRepository;
        this.passwordEncoder=passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        addAccountIfMissing("anna@example.com", "pw", Role.CUSTOMER);
        addAccountIfMissing("oliver@example.com", "pw", Role.STAFF);
    }

    private void addAccountIfMissing(String email, String password,
                                     Role role) {
        if (!accountRepository.existsByEmail(email)) {
            Account account = Account.create(email, passwordEncoder.encode(password), role);
            accountRepository.save(account);
        }
    }
}
