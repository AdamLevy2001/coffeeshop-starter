package ek.osnb.demo.coffeeshop.account;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/register")
class AccountController {

    private final AccountService accountService;

    AccountController(AccountService accountService) {
        this.accountService=accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void create(@Valid @RequestBody AccountRequest accountRequest) {
        accountService.create(accountRequest);
    }
}
