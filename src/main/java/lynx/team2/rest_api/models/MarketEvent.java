package lynx.team2.rest_api.models;

import lynx.team2.rest_api.LUtils;

public class MarketEvent {
    private String event_id;        // Unique identifier
    private String event_type;      // "BULL_RUN", "BEAR_CRASH", "SECTOR_BOOM", "SECTOR_SLUMP", "STOCK_SHOCK"
    private String scope;           // "MARKET", "SECTOR", or "STOCK"
    private String target;          // Sector name or ticker if scope is not MARKET. Null for MARKET scope
    private double magnitude;       // Price movement multiplier during the event (e.g. 1.5 = 50% stronger moves)
    private int duration_ticks;     // How many simulation ticks the event lasts
    private String headline;        // Human-readable news headline string (defined in seed)
    private long triggered_at;      // Simulated market timestamp of event start
    private String triggered_by;    // "SYSTEM" (automatic) or "ADMIN" (manual)

    public MarketEvent(String event_id, String event_type, String scope, String target, double magnitude, int duration_ticks, String headline, long triggered_at, String triggered_by) {
        this.event_id = event_id;
        this.event_type = event_type;
        this.scope = scope;
        this.target = target;
        this.magnitude = magnitude;
        this.duration_ticks = duration_ticks;
        this.headline = headline;
        this.triggered_at = triggered_at;
        this.triggered_by = triggered_by;
    }

    public String getEvent_id() { return event_id; }
    public String getEvent_type() { return event_type; }
    public String getScope() { return scope; }
    public String getTarget() { return target; }
    public double getMagnitude() { return magnitude; }
    public int getDuration_ticks() { return duration_ticks; }
    public String getHeadline() { return headline; }
    public String getTriggered_at() { return LUtils.longToIsoDate(triggered_at); }
    public String getTriggered_by() { return triggered_by; }

    public static MarketEvent getDummy(String event_id) {
        return new MarketEvent(
                event_id,
                "SECTOR_SLUMP",
                "SECTOR",
                "Tech",
                1.8,
                20,
                "Awesome Headline",
                1710511200L,
                "SYSTEM"
        );
    }
}
