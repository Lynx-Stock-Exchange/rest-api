package lynx.team2.rest_api.internal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PlatformAuthFilter extends OncePerRequestFilter {
    private final PlatformService platformService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PlatformAuthFilter(PlatformService platformService) {
        this.platformService = platformService;
    }

    @Value("${admin.secret-key}")
    private String adminToken;

    private void writeJsonError(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(
                Map.of("error", Map.of("code", code, "message", message, "details", Map.of()))
        ));
    }

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
            String adminTokenRequest = request.getHeader("X-Admin-Token");
            if (adminTokenRequest == null || !adminTokenRequest.equals(adminToken)) {
                writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "PLATFORM_NOT_AUTHORIZED", "Invalid or missing admin token.");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("API-KEY");
        String apiSecret = request.getHeader("API-SECRET");

        if (apiKey == null || apiSecret == null) {
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "PLATFORM_NOT_AUTHORIZED", "Missing API-KEY or API-SECRET headers.");
            return;
        }

        Platform platform = platformService.verify(apiKey, apiSecret);

        if (platform == null) {
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "PLATFORM_NOT_AUTHORIZED", "Invalid API credentials.");
            return;
        }

        // Attach platform to request for later use
        request.setAttribute("platform", platform);

        filterChain.doFilter(request, response);
    }
}