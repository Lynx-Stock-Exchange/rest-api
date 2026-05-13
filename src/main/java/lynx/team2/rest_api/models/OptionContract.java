package lynx.team2.rest_api.models;

import lynx.team2.rest_api.LUtils;

public class OptionContract {
    private String option_id;
    private String underlying_ticker;
    private String option_type; // "CALL", "PUT"
    private double strike_price;
    private long expiry_time;
    private double premium;
    private boolean is_active;
    private boolean auto_exercise;

    public OptionContract() {}

    public OptionContract(String option_id, String underlying_ticker, String option_type, double strike_price, long expiry_time, double premium, boolean is_active, boolean auto_exercise) {
        this.option_id = option_id;
        this.underlying_ticker = underlying_ticker;
        this.option_type = option_type;
        this.strike_price = strike_price;
        this.expiry_time = expiry_time;
        this.premium = premium;
        this.is_active = is_active;
        this.auto_exercise = auto_exercise;
    }

    public String getOption_id() { return option_id; }
    public String getUnderlying_ticker() { return underlying_ticker; }
    public String getOption_type() { return option_type; }
    public double getStrike_price() { return strike_price; }
    public String getExpiry_time() { return LUtils.longToIsoDate(expiry_time); }
    public double getPremium() { return premium; }
    public boolean isIs_active() { return is_active; }
    public boolean isAuto_exercise() { return auto_exercise; }

    public void setOption_id(String option_id) { this.option_id = option_id; }
    public void setUnderlying_ticker(String underlying_ticker) { this.underlying_ticker = underlying_ticker; }
    public void setOption_type(String option_type) { this.option_type = option_type; }
    public void setStrike_price(double strike_price) { this.strike_price = strike_price; }
    public void setExpiry_time(long expiry_time) { this.expiry_time = expiry_time; }
    public void setPremium(double premium) { this.premium = premium; }
    public void setIs_active(boolean is_active) { this.is_active = is_active; }
    public void setAuto_exercise(boolean auto_exercise) { this.auto_exercise = auto_exercise; }

    public static OptionContract getDummy(String option_id) {
        return new OptionContract(
                option_id,
                "ARKA",
                "CALL",
                150.0,
                1710511200L,
                5.0,
                true,
                true
        );
    }
}
