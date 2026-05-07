package lynx.team2.rest_api.repositories;

import lynx.team2.rest_api.entities.MarketStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketStateRepository extends JpaRepository<MarketStateEntity, Integer> {
}
