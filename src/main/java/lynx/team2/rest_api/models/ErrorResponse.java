package lynx.team2.rest_api.models;

public class ErrorResponse {
    /*
    {
        "error": {
            "code": "INSUFFICIENT_QUANTITY",
            "message": "Not enough shares available at current price.",
            "details": { }
        }
    }
     */

    static final int MARKET_CLOSED = 403;           // Order submitted while market is not open
    static final int INVALID_TICKER = 404;          // Ticker or option_id does not exist
    static final int INVALID_ORDER_TYPE = 400;      // Unknown order type submitted
    static final int INSUFFICIENT_QUANTITY = 400;   // Not enough shares at current price (market order)
    static final int INVALID_LIMIT_PRICE = 400;     // Limit price is missing or invalid
    static final int ORDER_SIZE_EXCEEDED = 400;     // Order quantity exceeds platform limit
    static final int OPTION_EXPIRED = 400;          //The option contract has expired
    static final int PLATFORM_NOT_AUTHORIZED = 401; // Invalid or missing API credentials
    static final int RATE_LIMIT_EXCEEDED = 429;     // Too many requests from this platform


}
