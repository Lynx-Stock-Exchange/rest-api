package lynx.team2.rest_api.models;

import lynx.team2.rest_api.LUtils;

public class MarketStatus {
    private boolean is_open;
    private long market_time;
    private long real_time;
    private int speed_multiplier;
    private String active_event_id;

    public MarketStatus() {}

    public MarketStatus(boolean is_open, long market_time, long real_time, int speed_multiplier, String active_event_id) {
        this.is_open = is_open;
        this.market_time = market_time;
        this.real_time = real_time;
        this.speed_multiplier = speed_multiplier;
        this.active_event_id = active_event_id;
    }

    public boolean isIs_open() { return is_open; }
    public String getMarket_time() { return LUtils.longToIsoDate(market_time); }
    public long getMarket_time_long() { return market_time; }
    public String getReal_time() { return LUtils.longToIsoDate(real_time); }
    public long getReal_time_long() { return real_time; }
    public int getSpeed_multiplier() { return speed_multiplier; }
    public String getActive_event_id() { return active_event_id; }

    public void setIs_open(boolean is_open) { this.is_open = is_open; }
    public void setMarket_time(long market_time) { this.market_time = market_time; }
    public void setReal_time(long real_time) { this.real_time = real_time; }
    public void setSpeed_multiplier(int speed_multiplier) { this.speed_multiplier = speed_multiplier; }
    public void setActive_event_id(String active_event_id) { this.active_event_id = active_event_id; }
}
