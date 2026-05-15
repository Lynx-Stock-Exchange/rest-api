package lynx.team2.rest_api.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class PlatformService {
    private final RestClient adminClient;

    public PlatformService(@Value("${admin-panel.url:http://admin-panel:8083}") String adminUrl) {
        this.adminClient = RestClient.builder().baseUrl(adminUrl).build();
    }

    public Platform verify(String apiKey, String apiSecret) {
        try {
            Map<String, Object> response = adminClient.post()
                    .uri("/api/v1/internal/platforms/verify")
                    .body(Map.of("api_key", apiKey, "api_secret", apiSecret))
                    .retrieve()
                    .body(Map.class);

            if (response != null && Boolean.TRUE.equals(response.get("valid"))) {
                return new Platform(
                        String.valueOf(response.get("platform_id")),
                        String.valueOf(response.get("platform_name")),
                        apiKey,
                        apiSecret
                );
            }
        } catch (Exception e) {
            // Log error or handle failure
        }
        return null;
    }
}
