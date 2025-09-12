package com.example.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-KEY";
    private final Set<String> validKeys;

    public ApiKeyAuthFilter(@Value("${app.api.keys}") String keys) {
        List<String> keyList = Arrays.stream(keys.split(","))
                                     .map(String::trim)
                                     .toList();
        this.validKeys = Set.copyOf(keyList);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String apiKey = req.getHeader(HEADER);

        if (apiKey == null || !validKeys.contains(apiKey)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"API key inválida o faltante\"}");
            return;
        }

        var auth = new UsernamePasswordAuthenticationToken(
                "api-key-user", null,
                List.of(new SimpleGrantedAuthority("ROLE_API"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(req, res);
    }
}

