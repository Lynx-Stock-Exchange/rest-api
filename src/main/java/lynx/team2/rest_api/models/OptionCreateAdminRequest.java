package lynx.team2.rest_api.models;

public class OptionCreateAdminRequest {
    private String underlying_ticker;
    private String option_type;
    private Double strike_price;
    private String expiry_time;
    private Double initial_premium;

    public String getUnderlying_ticker() { return underlying_ticker; }
    public String getOption_type() { return option_type; }
    public Double getStrike_price() { return strike_price; }
    public String getExpiry_time() { return expiry_time; }
    public Double getInitial_premium() { return initial_premium; }

    public void setUnderlying_ticker(String underlying_ticker) { this.underlying_ticker = underlying_ticker; }
    public void setOption_type(String option_type) { this.option_type = option_type; }
    public void setStrike_price(Double strike_price) { this.strike_price = strike_price; }
    public void setExpiry_time(String expiry_time) { this.expiry_time = expiry_time; }
    public void setInitial_premium(Double initial_premium) { this.initial_premium = initial_premium; }
}