package ek.osnb.demo.coffeeshop.account;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/me")
class MeController {

    record MeResponse(String username, List<String> roles) {}

    @GetMapping
    MeResponse me(Authentication auth) {

        String name = auth.getName();
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .filter(r -> r.startsWith("ROLE_"))
                .toList();

        return new MeResponse(name, roles);
    }
}
