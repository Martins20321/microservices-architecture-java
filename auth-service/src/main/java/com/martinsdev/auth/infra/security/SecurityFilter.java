package com.martinsdev.auth.infra.security;

import com.martinsdev.auth.infra.exception.ErrorResponse;
import com.martinsdev.auth.infra.exception.ForbiddenException;
import com.martinsdev.auth.infra.exception.InvalidTokenException;
import com.martinsdev.auth.service.AuthenticationService;
import com.martinsdev.auth.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // o filtro não deve ser executado em rotas liberadas
        if (!shouldNotFilter(request)) {
            try {
                String token = recuperarToken(request);

                if (token != null) {
                    String subject = tokenService.getSubject(token); // obtem o email do usuario

                    var usuario = authenticationService.loadUserByUsername(subject);

                    if (!usuario.isEnabled()) {
                        throw new ForbiddenException("Your account has been disabled. Contact an administrator");
                    }

                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()); // cria o usuario ja autenticado
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (InvalidTokenException | UsernameNotFoundException exception) { // token inválido ou usuario inexistente
                HttpStatus status = HttpStatus.UNAUTHORIZED;
                ErrorResponse errorResponse = new ErrorResponse(Instant.now(), status.value(), "Invalid token or User not found", exception.getMessage(), request.getRequestURI());
                String errorJson = objectMapper.writeValueAsString(errorResponse);
                response.setStatus(status.value());
                response.setContentType("application/json");
                response.getWriter().write(errorJson);
                return; // não executa o próximo filtro
            } catch (ForbiddenException exception) { // conta desabilitada
                HttpStatus status = HttpStatus.FORBIDDEN;
                ErrorResponse errorResponse = new ErrorResponse(Instant.now(), status.value(), "Access denied", exception.getMessage(), request.getRequestURI());
                String errorJson = objectMapper.writeValueAsString(errorResponse);
                response.setStatus(status.value());
                response.setContentType("application/json");
                response.getWriter().write(errorJson);
                return; // não executa o próximo filtro
            }
        }

        filterChain.doFilter(request, response);

    }

    private String recuperarToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization != null) {
            return authorization.strip().replace("Bearer ", "");
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI(); // caminha da requisicao

        return "/v1/auth/login".equals(path) || "/v1/auth/register".equals(path); // retorna true for alguma dessas rotas
    }
}
