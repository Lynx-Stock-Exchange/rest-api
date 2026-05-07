package lynx.team2.rest_api.models;

public class OrderRequest {
    private String platform_user_id;
    private String instrument_type;
    private String instrument_id;
    private String order_type;
    private String side;
    private Integer quantity;
    private Double limit_price;
    private String expires_at;

    public OrderRequest() {}

    public String getPlatform_user_id() { return platform_user_id; }
    public String getInstrument_type() { return instrument_type; }
    public String getInstrument_id() { return instrument_id; }
    public String getOrder_type() { return order_type; }
    public String getSide() { return side; }
    public Integer getQuantity() { return quantity; }
    public Double getLimit_price() { return limit_price; }
    public String getExpires_at() { return expires_at; }

    public void setPlatform_user_id(String platform_user_id) { this.platform_user_id = platform_user_id; }
    public void setInstrument_type(String instrument_type) { this.instrument_type = instrument_type; }
    public void setInstrument_id(String instrument_id) { this.instrument_id = instrument_id; }
    public void setOrder_type(String order_type) { this.order_type = order_type; }
    public void setSide(String side) { this.side = side; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setLimit_price(Double limit_price) { this.limit_price = limit_price; }
    public void setExpires_at(String expires_at) { this.expires_at = expires_at; }
}
