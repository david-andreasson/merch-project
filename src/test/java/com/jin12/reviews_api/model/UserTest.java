package com.jin12.reviews_api.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {
    @Test
    void testNoArgsConstructorAndSetters() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("testpass");

        List<Product> productList = new ArrayList<>();
        user.setProducts(productList);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo("testpass");
        assertThat(user.getProducts()).isSameAs(productList);
    }

    @Test
    void testAllArgsConstructor() {
        List<Product> productList = new ArrayList<>();
        User user = new User(1L, "user1", "password1", productList);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("user1");
        assertThat(user.getPassword()).isEqualTo("password1");
        assertThat(user.getProducts()).isSameAs(productList);
    }

    @Test
    void testBuilder() {
        List<Product> products = new ArrayList<>();
        User user = User.builder()
                .id(2L)
                .username("builderuser")
                .password("builderpass")
                .products(products)
                .build();

        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getUsername()).isEqualTo("builderuser");
        assertThat(user.getPassword()).isEqualTo("builderpass");
        assertThat(user.getProducts()).isSameAs(products);
    }

    @Test
    void testUserDetailsMethods() {
        User user = User.builder().build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertThat(authorities).isEmpty();
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void testEqualsAndHashCode() {
        User user1 = User.builder()
                .id(1L)
                .username("user1")
                .password("pass")
                .build();

        User user2 = User.builder()
                .id(1L)
                .username("user1")
                .password("pass")
                .build();

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void testToString() {
        User user = User.builder()
                .id(1L)
                .username("user1")
                .password("pass")
                .build();

        String toString = user.toString();

        assertThat(toString).contains("user1");
        assertThat(toString).contains("pass"); // Detta kan diskuteras pga säkerhet
    }
}