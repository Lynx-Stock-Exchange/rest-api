package lynx.team2.rest_api.exceptions;

public class ExchangeException extends RuntimeException {
    private final ErrorCode errorCode;

    public ExchangeException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
