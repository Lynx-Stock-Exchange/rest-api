package lynx.team2.rest_api.internal;

import lynx.team2.rest_api.repositories.PlatformRepository;
import org.springframework.stereotype.Service;

@Service
public class PlatformService {

    private final PlatformRepository platformRepository;

    public PlatformService(PlatformRepository platformRepository) {
        this.platformRepository = platformRepository;
    }

    public Platform verify(String apiKey, String apiSecret) {
        return platformRepository.findByApiKeyAndApiSecret(apiKey, apiSecret)
                .map(entity -> new Platform(entity.getId(), entity.getName(), entity.getApiKey(), entity.getApiSecret()))
                .orElse(null);
    }
}
