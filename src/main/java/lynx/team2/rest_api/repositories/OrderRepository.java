package lynx.team2.rest_api.repositories;

import lynx.team2.rest_api.entities.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    Optional<OrderEntity> findByOrderIdAndPlatformId(String orderId, String platformId);

    @Query("SELECT o FROM OrderEntity o WHERE o.platformId = :platformId " +
           "AND (:userId IS NULL OR o.platformUserId = :userId) " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:from IS NULL OR o.createdAt >= :from) " +
           "AND (:to IS NULL OR o.createdAt <= :to)")
    Page<OrderEntity> findByFilters(
            @Param("platformId") String platformId,
            @Param("userId") String userId,
            @Param("status") String status,
            @Param("from") Long from,
            @Param("to") Long to,
            Pageable pageable);

    @Query("SELECT o FROM OrderEntity o WHERE o.instrumentId = :instrumentId " +
           "AND o.orderType = 'LIMIT' " +
           "AND o.status IN ('PENDING', 'PARTIALLY_FILLED')")
    List<OrderEntity> findActiveOrdersForOrderBook(@Param("instrumentId") String instrumentId);

    @Query("SELECT o FROM OrderEntity o WHERE o.platformId = :platformId " +
           "AND o.status IN ('PENDING', 'PARTIALLY_FILLED')")
    List<OrderEntity> findActiveByPlatformId(@Param("platformId") String platformId);
}
