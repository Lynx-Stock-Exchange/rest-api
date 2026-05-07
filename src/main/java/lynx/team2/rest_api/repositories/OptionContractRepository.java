package lynx.team2.rest_api.repositories;

import lynx.team2.rest_api.entities.OptionContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionContractRepository extends JpaRepository<OptionContractEntity, String> {
    List<OptionContractEntity> findByIsActiveTrue();
}
