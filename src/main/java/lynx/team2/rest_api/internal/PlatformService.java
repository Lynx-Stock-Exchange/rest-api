package lynx.team2.rest_api.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Value("${admin.service.url}")
    private String adminServiceUrl;

    public Platform verify(String apiKey, String apiSecret) {
        try {
            String url = adminServiceUrl + "/internal/platforms/verify";
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
            System.err.println("[PlatformService] verify failed: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }
}
