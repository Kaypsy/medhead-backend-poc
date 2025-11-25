package com.medhead.bedallocation.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre d'authentification JWT qui intercepte chaque requête HTTP, extrait et
 * valide le token, puis peuple le SecurityContext si valide.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtProperties properties;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String headerName = properties.getHeader() != null ? properties.getHeader() : "Authorization";
        String prefix = properties.getPrefix() != null ? properties.getPrefix() : "Bearer ";

        try {
            String authHeader = request.getHeader(headerName);

            if (authHeader == null || authHeader.isBlank()) {
                log.debug("Aucun en-tête {} présent.", headerName);
                filterChain.doFilter(request, response);
                return;
            }

            if (!authHeader.startsWith(prefix)) {
                log.debug("En-tête {} présent mais sans préfixe attendu '{}'.", headerName, prefix);
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(prefix.length()).trim();

            if (token.isEmpty()) {
                log.debug("Token JWT vide après retrait du préfixe.");
                filterChain.doFilter(request, response);
                return;
            }

            // Éviter de ré-authentifier si déjà défini dans le contexte
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                if (tokenProvider.validateToken(token)) {
                    String username = tokenProvider.getUsernameFromToken(token);

                    if (username != null && !username.isBlank()) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Authentification JWT réussie pour l'utilisateur '{}'.", username);
                    } else {
                        log.debug("Impossible d'extraire un username valide du token.");
                    }
                } else {
                    log.debug("Token JWT invalide.");
                }
            }
        } catch (Exception e) {
            // Ne pas interrompre la chaîne de filtres en cas d'erreur
            log.warn("Erreur lors du traitement du filtre JWT: {}", e.getMessage());
        }

        // Poursuite de la chaîne de filtres dans tous les cas
        filterChain.doFilter(request, response);
    }
}
