package lynx.team2.rest_api.models;

import lynx.team2.rest_api.LUtils;

public class Trade {
    private String trade_id;            // Unique identifier for this execution
    private String order_id;            // The order this trade belongs to
    private String platform_id;         // The broker platform
    private String platform_user_id;    // The end user
    private String instrument_type;     // "STOCK" or "OPTION"
    private String instrument_id;       // Ticker or option_id
    private String side;                // "BUY" or "SELL"
    private int quantity;               // Shares/contracts executed in this fill
    private double price;                // Execution price per share/contract
    private double exchange_fee;         // Exchange fee for this execution (see Section 7)
    private long executed_at;           // Simulated market timestamp

    public Trade(String trade_id, String order_id, String platform_id, String platform_user_id, String instrument_type, String instrument_id, String side, int quantity, double price, double exchange_fee, long executed_at) {
        this.trade_id = trade_id;
        this.order_id = order_id;
        this.platform_id = platform_id;
        this.platform_user_id = platform_user_id;
        this.instrument_type = instrument_type;
        this.instrument_id = instrument_id;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.exchange_fee = exchange_fee;
        this.executed_at = executed_at;
    }

    public String getTrade_id() { return trade_id; }
    public String getOrder_id() { return order_id; }
    public String getPlatform_id() { return platform_id; }
    public String getPlatform_user_id() { return platform_user_id; }
    public String getInstrument_type() { return instrument_type; }
    public String getInstrument_id() { return instrument_id; }
    public String getSide() { return side; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public double getExchange_fee() { return exchange_fee; }
    public String getExecuted_at() { return LUtils.longToIsoDate(executed_at); }

    public static Trade getDummy(String trade_id) {
        return new Trade(
                trade_id,
                "order-abc-123",
                "platform-abc-123",
                "user-abc-123",
                "STOCK",
                "ARKA",
                "BUY",
                50,
                12.34,
                1.23,
                1776941245L
        );
    }
}
