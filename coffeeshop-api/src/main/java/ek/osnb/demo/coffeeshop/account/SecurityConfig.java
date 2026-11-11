package ek.osnb.demo.coffeeshop.account;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(csrf -> csrf.spa())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/products/**", "/api/categories/**", "/api/carts/**",
                                "/api/register", "/api/login", "/error").permitAll()
                        .requestMatchers("/api/staff/**", "/api/orders/*/ship").hasRole("STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/orders/*").hasAnyRole("STAFF", "CUSTOMER")
                        .requestMatchers("/api/customers/**", "/api/orders/**").hasRole("CUSTOMER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/api/login")
                        .successHandler((request, response, authentication) -> response.setStatus(HttpServletResponse.SC_NO_CONTENT))
                        .failureHandler((request, response, exception) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpServletResponse.SC_NO_CONTENT))
                )
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                );

        return http.build();

    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*@Bean
    UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        var u1 = User.builder()
                .username("staff@example.com")
                .password(passwordEncoder.encode("pw"))
                .roles("STAFF")
                .build();
        var u2 = User.builder()
                .username("anna@example.com")
                .password(passwordEncoder.encode("pw"))
                .roles("CUSTOMER")
                .build();
        return new InMemoryUserDetailsManager(u1, u2);
    }*/
}
