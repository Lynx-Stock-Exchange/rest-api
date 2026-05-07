package lynx.team2.rest_api.internal;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class PlatformService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${admin.service.url}")
    private String adminServiceUrl;

    public Platform verify(String apiKey, String apiSecret) {
        try {
            String url = adminServiceUrl + "/internal/platforms/verify";
            Map<String, String> body = Map.of("api_key", apiKey, "api_secret", apiSecret);

            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, body, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = response.getBody();
                boolean valid = json.path("valid").asBoolean(false);
                if (!valid) return null;

                String platformId = json.path("platform_id").asText(null);
                String platformName = json.path("platform_name").asText(null);
                if (platformId == null) return null;

                return new Platform(platformId, platformName);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
