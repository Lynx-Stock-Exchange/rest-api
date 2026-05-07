package lynx.team2.rest_api.internal;

import org.springframework.stereotype.Service;

@Service
public class PlatformService {
    // TEMP: replace with database later
    public Platform verify(String apiKey, String apiSecret) {

        // dummy validation (replace with DB lookup)
        if (apiKey.equals("test-key") && apiSecret.equals("test-secret")) {
            return new Platform("1", "Test Platform", apiKey, apiSecret);
        }
        return null;
    }
}
