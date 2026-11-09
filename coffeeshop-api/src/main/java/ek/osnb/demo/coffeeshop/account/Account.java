package ek.osnb.demo.coffeeshop.account;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    protected Account() {
    }

    private Account(String email, String password, Role role){
        this.email=email;
        this.password=password;
        this.role=role;
    }

    static Account create(String email, String password, Role role) {
        return new Account(email, password, role);
    }

    Long getId() {
        return id;
    }

    String getEmail() {
        return email;
    }

    String getPassword() {
        return password;
    }

    Role getRole() {
        return role;
    }
}
