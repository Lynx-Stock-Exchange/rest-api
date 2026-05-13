package lynx.team2.rest_api.models;

import lynx.team2.rest_api.LUtils;

public class OptionContract {
    private String option_id;           // Unique identifier for this option contract
    private String underlying_ticker;   // The stock this option is based on
    private String option_type;         // "CALL" or "PUT"
    private double strike_price;        // The price at which the option can be exercised
    private long expiry_time;           // Simulated market time at which the option expires
    private double premium;             // Current market price of the option contract itself
    private boolean is_active;          // Whether the option is still tradable
    private boolean auto_exercise;      // Always true: options auto-exercise at expiry if beneficial

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

    public static OptionContract getDummy(String option_id) {
        return new OptionContract(
                option_id,
                "ARKA",
                "CALL",
                12.34,
                1710511200L,
                1,
                true,
                true
        );
    }
}
