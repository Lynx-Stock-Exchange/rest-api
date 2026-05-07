package lynx.team2.rest_api.internal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class PlatformAuthFilter extends OncePerRequestFilter {
    private final PlatformService platformService;

    public PlatformAuthFilter(PlatformService platformService) {
        this.platformService = platformService;
    }

    @Value("${admin.secret-key}")
    private String adminToken;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Skip verification endpoint itself (important!)
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/internal/platforms/verify")) {
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println(path);

        if (path.startsWith("/api/v1/admin")) {
            System.out.println("ADMIN FILTER EXECUTED");
            String adminTokenRequest = request.getHeader("X-Admin-Token");

            if (adminTokenRequest == null || !adminTokenRequest.equals(adminToken)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Invalid admin token");
                return;
            } else {
                filterChain.doFilter(request, response);
                return;
            }
        } else {
            System.out.println("BASE FILTER EXECUTED");
        }

        String apiKey = request.getHeader("API-KEY");
        String apiSecret = request.getHeader("API-SECRET");

        if (apiKey == null || apiSecret == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing API credentials");
            return;
        }

        Platform platform = platformService.verify(apiKey, apiSecret);

        if (platform == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid API credentials");
            return;
        }

        // Attach platform to request for later use
        request.setAttribute("platform", platform);

        filterChain.doFilter(request, response);
    }
}