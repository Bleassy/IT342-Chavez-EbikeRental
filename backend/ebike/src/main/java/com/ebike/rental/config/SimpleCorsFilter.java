package com.ebike.rental.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SimpleCorsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String origin = request.getHeader("Origin");
        
        // Allow specific frontend domains and localhost for development
        if (origin != null && (
            origin.equals("https://it342-chavez-ebikerental-frontend.vercel.app") ||
            origin.equals("http://localhost:5173") ||
            origin.equals("http://localhost:3000") ||
            origin.equals("http://localhost:8081")
        )) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else if (origin != null) {
            // For debugging: log unexpected origins
            System.err.println("CORS: Unexpected origin: " + origin);
        }
        
        response.setHeader("Vary", "Origin");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,PATCH,HEAD");
        response.setHeader("Access-Control-Allow-Headers", "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,Cookie");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.flushBuffer();
            return;
        }

        filterChain.doFilter(request, response);
    }
}
