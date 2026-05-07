package lynx.team2.rest_api.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "trades")
public class TradeEntity {

    @Id
    @Column(name = "trade_id")
    private String tradeId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "platform_id", nullable = false)
    private String platformId;

    @Column(name = "platform_user_id", nullable = false)
    private String platformUserId;

    @Column(name = "instrument_type", nullable = false)
    private String instrumentType;

    @Column(name = "instrument_id", nullable = false)
    private String instrumentId;

    @Column(name = "side", nullable = false)
    private String side;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price")
    private Double price;

    @Column(name = "exchange_fee")
    private Double exchangeFee;

    @Column(name = "executed_at")
    private Long executedAt;

    public TradeEntity() {}

    public String getTradeId() { return tradeId; }
    public String getOrderId() { return orderId; }
    public String getPlatformId() { return platformId; }
    public String getPlatformUserId() { return platformUserId; }
    public String getInstrumentType() { return instrumentType; }
    public String getInstrumentId() { return instrumentId; }
    public String getSide() { return side; }
    public Integer getQuantity() { return quantity; }
    public Double getPrice() { return price; }
    public Double getExchangeFee() { return exchangeFee; }
    public Long getExecutedAt() { return executedAt; }

    public void setTradeId(String tradeId) { this.tradeId = tradeId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setPlatformId(String platformId) { this.platformId = platformId; }
    public void setPlatformUserId(String platformUserId) { this.platformUserId = platformUserId; }
    public void setInstrumentType(String instrumentType) { this.instrumentType = instrumentType; }
    public void setInstrumentId(String instrumentId) { this.instrumentId = instrumentId; }
    public void setSide(String side) { this.side = side; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setPrice(Double price) { this.price = price; }
    public void setExchangeFee(Double exchangeFee) { this.exchangeFee = exchangeFee; }
    public void setExecutedAt(Long executedAt) { this.executedAt = executedAt; }
}
