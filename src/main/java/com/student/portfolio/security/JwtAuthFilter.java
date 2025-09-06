package com.student.portfolio.security;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }	

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    	System.err.println("working");

    	String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
        	System.err.println(header);
            String token = header.substring(7);
            try {
            	if (jwtUtil.validate(token)) {
            	    String username = jwtUtil.getSubject(token);
            	    String role = jwtUtil.getRole(token); // will always be ROLE_USER

            	    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            	    UsernamePasswordAuthenticationToken auth =
            	            new UsernamePasswordAuthenticationToken(
            	                    userDetails,
            	                    null,
            	                    Collections.singletonList(new SimpleGrantedAuthority(role))
            	            );

            	    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            	    SecurityContextHolder.getContext().setAuthentication(auth);
            	}

            } catch (Exception e) {
                log.warn("JWT processing failed: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}