package lynx.team2.rest_api.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "option_contracts")
public class OptionContractEntity {

    @Id
    @Column(name = "option_id")
    private String optionId;

    @Column(name = "underlying_ticker", nullable = false, length = 5)
    private String underlyingTicker;

    @Column(name = "option_type", nullable = false)
    private String optionType;

    @Column(name = "strike_price")
    private Double strikePrice;

    @Column(name = "expiry_time")
    private Long expiryTime;

    @Column(name = "premium")
    private Double premium;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "auto_exercise")
    private Boolean autoExercise;

    public OptionContractEntity() {}

    public String getOptionId() { return optionId; }
    public String getUnderlyingTicker() { return underlyingTicker; }
    public String getOptionType() { return optionType; }
    public Double getStrikePrice() { return strikePrice; }
    public Long getExpiryTime() { return expiryTime; }
    public Double getPremium() { return premium; }
    public Boolean getIsActive() { return isActive; }
    public Boolean getAutoExercise() { return autoExercise; }

    public void setOptionId(String optionId) { this.optionId = optionId; }
    public void setUnderlyingTicker(String underlyingTicker) { this.underlyingTicker = underlyingTicker; }
    public void setOptionType(String optionType) { this.optionType = optionType; }
    public void setStrikePrice(Double strikePrice) { this.strikePrice = strikePrice; }
    public void setExpiryTime(Long expiryTime) { this.expiryTime = expiryTime; }
    public void setPremium(Double premium) { this.premium = premium; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setAutoExercise(Boolean autoExercise) { this.autoExercise = autoExercise; }
}
