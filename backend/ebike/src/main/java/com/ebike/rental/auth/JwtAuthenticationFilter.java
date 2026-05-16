package com.ebike.rental.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ebike.rental.dto.ApiResponse;
import com.ebike.rental.user.JwtUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String requestPath = request.getRequestURI();
            logger.debug("🔍 JWT Filter processing: {} {}", request.getMethod(), requestPath);
            
            String jwt = extractTokenFromRequest(request);
            
            if (jwt != null) {
                logger.debug("✅ JWT token found in request");
                
                if (jwtTokenProvider.isTokenValid(jwt)) {
                    logger.debug("✅ JWT token is valid");
                    
                    String email = jwtTokenProvider.getEmailFromToken(jwt);
                    Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
                    String role = jwtTokenProvider.getRoleFromToken(jwt);
                    
                    logger.debug("✅ JWT decoded - userId: {}, email: {}, role: {}", userId, email, role);

                    List<GrantedAuthority> authorities = new ArrayList<>();
                    if (role != null) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(email, null, authorities);
                    authentication.setDetails(new JwtUserDetails(userId, email, role));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("✅ Authentication set in SecurityContext for: {}", email);
                } else {
                    logger.warn("❌ JWT token validation failed");
                }
            } else {
                logger.debug("⚠️ No JWT token found in Authorization header");
            }
        } catch (Exception e) {
            logger.error("❌ Error in JWT authentication filter", e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
