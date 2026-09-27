package com.app.vendia.service;

import com.app.vendia.model.Client;
import com.app.vendia.model.Role;
import com.app.vendia.dto.request.*;
import com.app.vendia.dto.response.*;
import com.app.vendia.exception.EmailDejaUtiliseException;
import com.app.vendia.exception.IdentifiantsInvalidesException;
import com.app.vendia.repository.ClientRepository;
import com.app.vendia.security.JwtService;
import com.app.vendia.security.VendiaUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse inscrire(RegisterRequest requete) {
        if (clientRepository.existsByEmail(requete.email())) {
            throw new EmailDejaUtiliseException(requete.email());
        }

        Client client = Client.builder()
                .nom(requete.nom())
                .prenom(requete.prenom())
                .email(requete.email())
                .telephone(requete.telephone())
                .motDePasse(passwordEncoder.encode(requete.motDePasse()))
                .role(Role.ROLE_CLIENT)
                .build();

        clientRepository.save(client);

        VendiaUserDetails userDetails = new VendiaUserDetails(client);
        return AuthResponse.of(
                jwtService.genererAccessToken(userDetails),
                jwtService.genererRefreshToken(userDetails)
        );
    }

    public AuthResponse connecter(LoginRequest requete) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requete.email(), requete.motDePasse())
            );
        } catch (BadCredentialsException e) {
            throw new IdentifiantsInvalidesException();
        }

        Client client = clientRepository.findByEmail(requete.email())
                .orElseThrow(IdentifiantsInvalidesException::new);

        VendiaUserDetails userDetails = new VendiaUserDetails(client);
        return AuthResponse.of(
                jwtService.genererAccessToken(userDetails),
                jwtService.genererRefreshToken(userDetails)
        );
    }

    public AuthResponse rafraichir(RefreshRequest requete) {
        String email = jwtService.extraireEmail(requete.refreshToken());
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(IdentifiantsInvalidesException::new);

        VendiaUserDetails userDetails = new VendiaUserDetails(client);
        if (!jwtService.estValide(requete.refreshToken(), userDetails)) {
            throw new IdentifiantsInvalidesException();
        }

        // Le refresh token reste inchangé ; seul un nouvel access token est émis.
        return AuthResponse.of(
                jwtService.genererAccessToken(userDetails),
                requete.refreshToken()
        );
    }
}
