package lynx.team2.rest_api.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "platform_id", nullable = false)
    private String platformId;

    @Column(name = "platform_user_id", nullable = false)
    private String platformUserId;

    @Column(name = "instrument_type", nullable = false)
    private String instrumentType;

    @Column(name = "instrument_id", nullable = false)
    private String instrumentId;

    @Column(name = "order_type", nullable = false)
    private String orderType;

    @Column(name = "side", nullable = false)
    private String side;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "limit_price")
    private Double limitPrice;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "filled_quantity")
    private Integer filledQuantity;

    @Column(name = "average_fill_price")
    private Double averageFillPrice;

    @Column(name = "exchange_fee")
    private Double exchangeFee;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @Column(name = "expires_at")
    private Long expiresAt;

    public OrderEntity() {}

    public String getOrderId() { return orderId; }
    public String getPlatformId() { return platformId; }
    public String getPlatformUserId() { return platformUserId; }
    public String getInstrumentType() { return instrumentType; }
    public String getInstrumentId() { return instrumentId; }
    public String getOrderType() { return orderType; }
    public String getSide() { return side; }
    public Integer getQuantity() { return quantity; }
    public Double getLimitPrice() { return limitPrice; }
    public String getStatus() { return status; }
    public Integer getFilledQuantity() { return filledQuantity; }
    public Double getAverageFillPrice() { return averageFillPrice; }
    public Double getExchangeFee() { return exchangeFee; }
    public Long getCreatedAt() { return createdAt; }
    public Long getUpdatedAt() { return updatedAt; }
    public Long getExpiresAt() { return expiresAt; }

    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setPlatformId(String platformId) { this.platformId = platformId; }
    public void setPlatformUserId(String platformUserId) { this.platformUserId = platformUserId; }
    public void setInstrumentType(String instrumentType) { this.instrumentType = instrumentType; }
    public void setInstrumentId(String instrumentId) { this.instrumentId = instrumentId; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public void setSide(String side) { this.side = side; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setLimitPrice(Double limitPrice) { this.limitPrice = limitPrice; }
    public void setStatus(String status) { this.status = status; }
    public void setFilledQuantity(Integer filledQuantity) { this.filledQuantity = filledQuantity; }
    public void setAverageFillPrice(Double averageFillPrice) { this.averageFillPrice = averageFillPrice; }
    public void setExchangeFee(Double exchangeFee) { this.exchangeFee = exchangeFee; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
}
