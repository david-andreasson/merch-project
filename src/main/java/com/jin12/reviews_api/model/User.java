package com.jin12.reviews_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User entity implementing UserDetails for authentication.
 * encryptedApiKey stores the user's API key in encrypted form.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    /**
     * Stores the user's API key in encrypted form for API_KEY authentication flow.
     */
    @Column(name = "encrypted_api_key")
    private String encryptedApiKey;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // No roles/authorities used in this application
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Not tracking account expiration
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Not implementing account locking
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Not implementing credential expiration
    }

    @Override
    public boolean isEnabled() {
        return true; // All users remain enabled by default
    }
}