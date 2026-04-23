package lynx.team2.rest_api.models;

public class PortfolioPosition {
    private String platform_id;         // The broker platform
    private String platform_user_id;    // The end user
    private String instrument_type;     // "STOCK" or "OPTION"
    private String instrument_id;       // Ticker or option_id
    private int quantity;               // Current holding
    private double average_cost;         // Average purchase price (for P&L calculation)

    public PortfolioPosition(String platform_id, String platform_user_id, String instrument_type, String instrument_id, int quantity, double average_cost) {
        this.platform_id = platform_id;
        this.platform_user_id = platform_user_id;
        this.instrument_type = instrument_type;
        this.instrument_id = instrument_id;
        this.quantity = quantity;
        this.average_cost = average_cost;
    }

    public String getPlatform_id() { return platform_id; }
    public String getPlatform_user_id() { return platform_user_id; }
    public String getInstrument_type() { return instrument_type; }
    public String getInstrument_id() { return instrument_id; }
    public int getQuantity() { return quantity; }
    public double getAverage_cost() { return average_cost; }
}
