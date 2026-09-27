package com.app.vendia.security;

import com.app.vendia.model.Client;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/** Adapte l'entité Client au contrat UserDetails attendu par Spring Security. */
public class VendiaUserDetails implements UserDetails {

    private final Client client;

    public VendiaUserDetails(Client client) {
        this.client = client;
    }

    public Long getClientId() {
        return client.getId();
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(client.getRole().name()));
    }

    @Override
    public String getPassword() {
        return client.getMotDePasse();
    }

    @Override
    public String getUsername() {
        return client.getEmail();
    }

    @Override 
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override 
    public boolean isCredentialsNonExpired() { return true; }

    @Override 
    public boolean isEnabled() { return true; }
    
}
