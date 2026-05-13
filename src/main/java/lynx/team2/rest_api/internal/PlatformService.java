package lynx.team2.rest_api.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lynx.team2.rest_api.state.StateStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class PlatformService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StateStore stateStore;

    @Value("${admin.service.url}")
    private String adminServiceUrl;

    public PlatformService(StateStore stateStore) {
        this.stateStore = stateStore;
    }

    public Platform verify(String apiKey, String apiSecret) {
        // Check locally-registered platforms first (created via REST API admin endpoint)
        Platform local = stateStore.verifyPlatformCredentials(apiKey, apiSecret);
        if (local != null) {
            return local;
        }

        // Fall back to admin panel for platforms created via the Admin Panel UI
        try {
            String url = adminServiceUrl + "/api/v1/internal/platforms/verify";
            Map<String, String> body = Map.of("api_key", apiKey, "api_secret", apiSecret);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                boolean valid = json.path("valid").asBoolean(false);
                if (!valid) return null;

                String platformId = json.path("platform_id").asText(null);
                String platformName = json.path("platform_name").asText(null);
                if (platformId == null) return null;

                return new Platform(platformId, platformName);
            }
        } catch (Exception e) {
            System.err.println("[PlatformService] Admin panel verify failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return null;
    }
}