package lynx.team2.rest_api.exceptions;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    MARKET_CLOSED(HttpStatus.FORBIDDEN),
    INVALID_TICKER(HttpStatus.NOT_FOUND),
    INVALID_ORDER_TYPE(HttpStatus.BAD_REQUEST),
    INSUFFICIENT_QUANTITY(HttpStatus.BAD_REQUEST),
    INVALID_LIMIT_PRICE(HttpStatus.BAD_REQUEST),
    ORDER_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST),
    OPTION_EXPIRED(HttpStatus.BAD_REQUEST),
    PLATFORM_NOT_AUTHORIZED(HttpStatus.UNAUTHORIZED),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND),
    ORDER_NOT_CANCELLABLE(HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
