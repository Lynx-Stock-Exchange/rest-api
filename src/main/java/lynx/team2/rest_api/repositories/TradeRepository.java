package lynx.team2.rest_api.repositories;

import lynx.team2.rest_api.entities.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, String> {
    List<TradeEntity> findByPlatformIdAndPlatformUserId(String platformId, String platformUserId);
}
