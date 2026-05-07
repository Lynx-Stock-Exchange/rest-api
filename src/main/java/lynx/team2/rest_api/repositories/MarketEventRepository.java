package lynx.team2.rest_api.repositories;

import lynx.team2.rest_api.entities.MarketEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketEventRepository extends JpaRepository<MarketEventEntity, String> {

    @Query("SELECT e FROM MarketEventEntity e ORDER BY e.triggeredAt DESC")
    List<MarketEventEntity> findRecentEvents();
}
