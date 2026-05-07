package lynx.team2.rest_api.repositories;

import lynx.team2.rest_api.entities.PlatformEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlatformRepository extends JpaRepository<PlatformEntity, String> {
    Optional<PlatformEntity> findByApiKeyAndApiSecret(String apiKey, String apiSecret);
}
