package lynx.team2.rest_api.repositories;

import lynx.team2.rest_api.entities.OHLCPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OHLCPointRepository extends JpaRepository<OHLCPointEntity, Long> {

    @Query("SELECT o FROM OHLCPointEntity o WHERE o.ticker = :ticker " +
           "AND (:intervalType IS NULL OR o.intervalType = :intervalType) " +
           "AND (:from IS NULL OR o.timestamp >= :from) " +
           "AND (:to IS NULL OR o.timestamp <= :to) " +
           "ORDER BY o.timestamp ASC")
    List<OHLCPointEntity> findByTickerAndFilters(
            @Param("ticker") String ticker,
            @Param("intervalType") String intervalType,
            @Param("from") Long from,
            @Param("to") Long to);
}
