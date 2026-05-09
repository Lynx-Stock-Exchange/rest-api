package lynx.team2.rest_api.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "fee_config")
public class FeeConfigEntity {

    @Id
    @Column(name = "id")
    private Integer id = 1;

    @Column(name = "fee_rate", nullable = false)
    private Double feeRate = 0.001;

    public FeeConfigEntity() {}

    public Integer getId() { return id; }
    public Double getFeeRate() { return feeRate; }

    public void setId(Integer id) { this.id = id; }
    public void setFeeRate(Double feeRate) { this.feeRate = feeRate; }
}
