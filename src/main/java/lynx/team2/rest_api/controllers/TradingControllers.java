package lynx.team2.rest_api.controllers;

import lynx.team2.rest_api.models.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class TradingControllers {

    private final RestClient engineClient;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String engineBaseUrl;

    public TradingControllers(
            @Value("${order-book-engine.url:http://order-book-engine:8086}") String engineBaseUrl) {
        this.engineBaseUrl = engineBaseUrl;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.engineClient = RestClient.builder()
                .baseUrl(engineBaseUrl)
                .build();
    }

    /**
     * POST /orders <br>
     * Submit a new order. Forwards the request body to the order-book-engine
     * and returns its response (including the assigned {@code order_id}) verbatim.
     */
    @PostMapping("")
    public ResponseEntity<String> postOrder(@RequestBody Map<String, Object> payload) {
        try {
            byte[] bodyBytes = objectMapper.writeValueAsBytes(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(engineBaseUrl + "/orders"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return ResponseEntity.status(HttpStatusCode.valueOf(resp.statusCode()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resp.body());
        } catch (Exception e) {
            return ResponseEntity.status(502)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":{\"code\":\"ENGINE_UNREACHABLE\",\"message\":\"" + e.getMessage() + "\"}}");
        }
    }

    /**
     * GET /orders/:order_id <br>
     * Fetches the current state of an order from the order-book-engine.
     */
    @GetMapping("/{order_id}")
    public ResponseEntity<String> getOrderById(@PathVariable("order_id") String order_id) {
        return proxy(() -> engineClient.get()
                .uri("/orders/{id}", order_id)
                .retrieve()
                .toEntity(String.class));
    }

    /**
     * DELETE /orders/:order_id <br>
     * Cancels a {@code PENDING} or {@code PARTIALLY_FILLED} order via the
     * order-book-engine.
     */
    @DeleteMapping("/{order_id}")
    public ResponseEntity<String> deleteOrderById(@PathVariable("order_id") String order_id) {
        return proxy(() -> engineClient.delete()
                .uri("/orders/{id}", order_id)
                .retrieve()
                .toEntity(String.class));
    }

    /**
     * GET /orders <br>
     * Paginated order history for the platform. <br>
     * TODO: The order-book-engine does not yet expose a list endpoint;
     * brokers should query their own order DB for history. Returns an empty
     * list for now instead of fabricated data.
     */
    @GetMapping("")
    public List<Order> getOrders(
            @RequestParam(value = "platform_user_id", required = false) String platform_user_id,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page_size", required = false) Integer page_size) {
        return new ArrayList<>();
    }

    private ResponseEntity<String> proxy(EngineCall call) {
        try {
            ResponseEntity<String> resp = call.execute();
            return ResponseEntity.status(resp.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resp.getBody());
        } catch (RestClientResponseException e) {
            HttpStatusCode code = e.getStatusCode();
            String body = e.getResponseBodyAsString();
            return ResponseEntity.status(code)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body == null || body.isEmpty() ? "{}" : body);
        } catch (ResourceAccessException e) {
            String msg = e.getMessage() == null ? "unreachable" : e.getMessage().replace("\"", "'");
            return ResponseEntity.status(502)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":{\"code\":\"ENGINE_UNREACHABLE\",\"message\":\"" + msg + "\"}}");
        }
    }

    @FunctionalInterface
    private interface EngineCall {
        ResponseEntity<String> execute();
    }
}
