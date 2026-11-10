package ek.osnb.demo.coffeeshop.account;

import ek.osnb.demo.coffeeshop.common.ConflictException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository=accountRepository;
        this.passwordEncoder=passwordEncoder;
    }

    Account create(AccountRequest accountRequest) {
        if (accountRepository.existsByEmail(accountRequest.email())) {
            throw new ConflictException("Account already exist");
        }
        return accountRepository.save(Account.create(accountRequest.email(),
                passwordEncoder.encode(accountRequest.password()),
                Role.CUSTOMER));
    }
}
