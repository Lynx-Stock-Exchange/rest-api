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
    private final String expectedAdminToken;

    public PlatformAuthFilter(
            PlatformService platformService,
            @Value("${exchange.admin-token:test-token}") String expectedAdminToken
    ) {
        this.platformService = platformService;
        this.expectedAdminToken = expectedAdminToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Skip verification endpoint itself (important!)
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/internal/platforms/verify")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/api/v1/admin")) {
            String adminToken = request.getHeader("ADMIN-TOKEN");

            if (adminToken == null || !adminToken.equals(expectedAdminToken)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Invalid admin token");
                return;
            }

            filterChain.doFilter(request, response);
            return;
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
