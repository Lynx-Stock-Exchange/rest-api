package lynx.team2.rest_api.models;

public class OrderBookPoint {
    private double price;
    private int quantity;

    public OrderBookPoint(double price, int quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public double getPrice() { return price; }

    public int getQuantity() { return quantity; }

    public static OrderBookPoint getDummy(int offset) {
        return new OrderBookPoint(
                12.34 + offset,
                5 + offset
        );
    }
}