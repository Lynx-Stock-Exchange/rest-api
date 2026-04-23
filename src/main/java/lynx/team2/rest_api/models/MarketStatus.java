package lynx.team2.rest_api.models;

public class MarketStatus {
    private boolean is_open;
    private String market_time;
    private String real_time;
    private int speed_multiplier;
    private String active_event;

    public MarketStatus(boolean is_open, String market_time, String real_time, int speed_multiplier, String active_event) {
        this.is_open = is_open;
        this.market_time = market_time;
        this.real_time = real_time;
        this.speed_multiplier = speed_multiplier;
        this.active_event = active_event;
    }

    public boolean isIs_open() { return is_open; }
    public String getMarket_time() { return market_time; }
    public String getReal_time() { return real_time; }
    public int getSpeed_multiplier() { return speed_multiplier; }
    public String getActive_event() { return active_event; }
}
