package in.souravkadam.foodiesapi.filters;

import in.souravkadam.foodiesapi.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Only skip auth for public endpoints — cart/orders/etc MUST go through
        return path.equals("/api/login")
                || path.equals("/api/register")
                || path.startsWith("/api/foods")
                || path.startsWith("/uploads");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Allow CORS preflight through without auth check
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        System.out.println("JWT FILTER — path: " + request.getServletPath()
                + " | header: " + (authHeader != null ? "present" : "MISSING"));

        // No token — let Spring Security return 401 naturally
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtUtil.extractUsername(token);

            if (email != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("JWT FILTER — auth set for: " + email);
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            System.out.println("JWT FILTER — token expired for path: " + request.getServletPath());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "TOKEN_EXPIRED", "Your session has expired. Please log in again.");

        } catch (MalformedJwtException | SignatureException e) {
            System.out.println("JWT FILTER — invalid token: " + e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "TOKEN_INVALID", "Invalid token. Please log in again.");

        } catch (Exception e) {
            System.out.println("JWT FILTER — unexpected error: " + e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "AUTH_ERROR", "Authentication error: " + e.getMessage());
        }
    }

    private void sendErrorResponse(HttpServletResponse response,
                                   int status, String error, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                String.format("{\"error\":\"%s\",\"message\":\"%s\"}", error, message)
        );
    }
}
