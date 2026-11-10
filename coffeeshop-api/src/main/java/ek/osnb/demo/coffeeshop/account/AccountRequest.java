package ek.osnb.demo.coffeeshop.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record AccountRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8)
        String password
) {
}
