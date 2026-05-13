package lynx.team2.rest_api.models;

import lynx.team2.rest_api.LUtils;

public class OHLCPoint {
    private Double current_price;   // Current market price (decimal, 2dp)
    private Double open_price;      // Price at market open for the current simulated day
    private Double high_price;      // Highest price in the current simulated day
    private Double low_price;       // Lowest price in the current simulated day
    private Integer volume;             // Total shares traded in current simulated day
    private Long timestamp;         // Market timestamp

    public OHLCPoint(
            double current_price,
            double open_price,
            double high_price,
            double low_price,
            int volume,
            Long timestamp) {
        this.current_price = current_price;
        this.open_price = open_price;
        this.high_price = high_price;
        this.low_price = low_price;
        this.volume = volume;
        this.timestamp = timestamp;
    }

    public double getCurrent_price() { return current_price; }
    public double getOpen_price() { return open_price; }
    public double getHigh_price() { return high_price; }
    public double getLow_price() { return low_price; }
    public int getVolume() { return volume; }
    public String getTimestamp() { return LUtils.longToIsoDate(timestamp); }

    public static OHLCPoint getDummy(int offset) {
        return new OHLCPoint(
                12.34 + offset,
                23.45 + offset,
                34.56 + offset,
                45.67 + offset,
                123,
                1776941245L + (offset*10L)
        );
    }
}
