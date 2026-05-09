package lynx.team2.rest_api.models;

public class OptionUpdateRequest {
    private Double strike_price;
    private String expiry_time;
    private Double premium;

    public Double getStrike_price() { return strike_price; }
    public String getExpiry_time() { return expiry_time; }
    public Double getPremium() { return premium; }

    public void setStrike_price(Double strike_price) { this.strike_price = strike_price; }
    public void setExpiry_time(String expiry_time) { this.expiry_time = expiry_time; }
    public void setPremium(Double premium) { this.premium = premium; }
}