package lynx.team2.rest_api.models;

public class OptionUpdateRequest {
    private Double strike_price;
    private String expiry_time;
    private Double premium;
    private Boolean is_active;

    public Double getStrike_price() { return strike_price; }
    public String getExpiry_time() { return expiry_time; }
    public Double getPremium() { return premium; }
    public Boolean getIs_active() { return is_active; }

    public void setStrike_price(Double strike_price) { this.strike_price = strike_price; }
    public void setExpiry_time(String expiry_time) { this.expiry_time = expiry_time; }
    public void setPremium(Double premium) { this.premium = premium; }
    public void setIs_active(Boolean is_active) { this.is_active = is_active; }
}