package lynx.team2.rest_api.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExchangeException.class)
    public ResponseEntity<Map<String, Object>> handleExchangeException(ExchangeException ex) {
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(Map.of("error", Map.of(
                        "code", ex.getErrorCode().name(),
                        "message", ex.getMessage(),
                        "details", Map.of()
                )));
    }
}
