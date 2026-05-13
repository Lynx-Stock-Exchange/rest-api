package lynx.team2.rest_api.models;

import java.util.List;

public class OrderBook {
    private String ticker;
    private List<OrderBookPoint> bids;
    private List<OrderBookPoint> asks;

    public OrderBook(
            String ticker,
            List<OrderBookPoint> bids,
            List<OrderBookPoint> asks) {
        this.ticker = ticker;
        this.bids = bids;
        this.asks = asks;
    }

    public String getTicker() { return ticker; }
    public List<OrderBookPoint> getBids() { return bids; }
    public List<OrderBookPoint> getAsks() { return asks; }
}