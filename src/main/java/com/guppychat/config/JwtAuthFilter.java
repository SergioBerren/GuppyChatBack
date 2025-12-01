package com.guppychat.config;

import com.guppychat.repository.UsuarioRepositorio;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UsuarioRepositorio usuarioRepositorio;
    
    public JwtAuthFilter(JwtUtil jwtUtil, UsuarioRepositorio usuarioRepositorio) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepositorio = usuarioRepositorio;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        System.out.println("🔍 JwtAuthFilter - URI: " + requestURI);
        
        String header = request.getHeader("Authorization");
        
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            System.out.println("🔑 Token recibido: " + token.substring(0, Math.min(20, token.length())) + "...");
            
            try {
                if (jwtUtil.validarToken(token)) {
                    String correo = jwtUtil.obtenerCorreo(token);
                    System.out.println("✅ Token válido para: " + correo);
                    
                    usuarioRepositorio.findByCorreo(correo).ifPresent(usuario -> {
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                new User(usuario.getCorreo(), usuario.getPassword(), Collections.emptyList()),
                                null,
                                Collections.emptyList()
                        );
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        System.out.println("✅ Autenticación establecida para: " + correo);
                    });
                } else {
                    System.out.println("❌ Token inválido");
                }
            } catch (Exception e) {
                System.out.println("❌ Error al validar token: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ No se encontró header Authorization o no empieza con 'Bearer '");
        }
        
        chain.doFilter(request, response);
    }
}