package lynx.team2.rest_api.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lynx.team2.rest_api.exceptions.ErrorCode;
import lynx.team2.rest_api.exceptions.ExchangeException;
import lynx.team2.rest_api.internal.Platform;
import lynx.team2.rest_api.kafka.KafkaProducerService;
import lynx.team2.rest_api.models.Order;
import lynx.team2.rest_api.models.OrderRequest;
import lynx.team2.rest_api.state.StateStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
public class TradingControllers {

    private final StateStore stateStore;
    private final KafkaProducerService kafkaProducerService;

    public TradingControllers(StateStore stateStore, KafkaProducerService kafkaProducerService) {
        this.stateStore = stateStore;
        this.kafkaProducerService = kafkaProducerService;
    }

    @PostMapping("")
    public Order postOrder(@RequestBody OrderRequest req, HttpServletRequest request) {
        Platform platform = (Platform) request.getAttribute("platform");

        // Market must be open
        if (!stateStore.getMarketStatus().isIs_open()) {
            throw new ExchangeException(ErrorCode.MARKET_CLOSED, "The market is currently closed.");
        }

        // Validate instrument
        validateInstrument(req.getInstrument_type(), req.getInstrument_id());

        // Basic validation
        if (req.getPlatform_user_id() == null || req.getPlatform_user_id().isBlank()) {
            throw new ExchangeException(ErrorCode.INVALID_REQUEST, "platform_user_id is required.");
        }
        if (req.getQuantity() == null || req.getQuantity() <= 0) {
            throw new ExchangeException(ErrorCode.INVALID_REQUEST, "quantity must be a positive integer.");
        }

        String orderId = UUID.randomUUID().toString();
        
        // Prepare Kafka message
        Map<String, Object> orderMsg = new HashMap<>();
        orderMsg.put("order_id", orderId);
        orderMsg.put("platform_id", platform.getId());
        orderMsg.put("platform_user_id", req.getPlatform_user_id());
        orderMsg.put("instrument_type", req.getInstrument_type());
        orderMsg.put("instrument_id", req.getInstrument_id());
        orderMsg.put("order_type", req.getOrder_type());
        orderMsg.put("side", req.getSide());
        orderMsg.put("quantity", req.getQuantity());
        orderMsg.put("limit_price", req.getLimit_price());
        orderMsg.put("expires_at", req.getExpires_at());

        kafkaProducerService.sendOrderRequest(orderMsg);

        // Return a PENDING order immediately (stateless)
        // Note: In a real system, the client might wait for the event or poll.
        Order pendingOrder = new Order();
        pendingOrder.setOrder_id(orderId);
        pendingOrder.setPlatform_id(platform.getId());
        pendingOrder.setPlatform_user_id(req.getPlatform_user_id());
        pendingOrder.setInstrument_type(req.getInstrument_type());
        pendingOrder.setInstrument_id(req.getInstrument_id());
        pendingOrder.setOrder_type(req.getOrder_type());
        pendingOrder.setSide(req.getSide());
        pendingOrder.setQuantity(req.getQuantity());
        pendingOrder.setLimit_price(req.getLimit_price());
        pendingOrder.setStatus("PENDING");
        pendingOrder.setFilled_quantity(0);
        
        // We don't save to state store here, we wait for the loopback from Kafka
        return pendingOrder;
    }

    @GetMapping("/{order_id}")
    public Order getOrderById(@PathVariable("order_id") String orderId, HttpServletRequest request) {
        Platform platform = (Platform) request.getAttribute("platform");

        return stateStore.getOrder(orderId)
                .filter(o -> platform.getId().equals(o.getPlatform_id()))
                .orElseThrow(() -> new ExchangeException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderId));
    }

    @DeleteMapping("/{order_id}")
    public ResponseEntity<Void> deleteOrderById(@PathVariable("order_id") String orderId, HttpServletRequest request) {
        Platform platform = (Platform) request.getAttribute("platform");

        Order order = stateStore.getOrder(orderId)
                .filter(o -> platform.getId().equals(o.getPlatform_id()))
                .orElseThrow(() -> new ExchangeException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderId));

        if (!"PENDING".equals(order.getStatus()) && !"PARTIALLY_FILLED".equals(order.getStatus())) {
            throw new ExchangeException(ErrorCode.ORDER_NOT_CANCELLABLE,
                    "Order cannot be cancelled in status: " + order.getStatus());
        }

        Map<String, Object> cancelMsg = new HashMap<>();
        cancelMsg.put("action", "CANCEL_ORDER");
        cancelMsg.put("order_id", orderId);
        cancelMsg.put("platform_id", platform.getId());

        kafkaProducerService.sendOrderRequest(cancelMsg);

        return ResponseEntity.accepted().build();
    }

    @GetMapping("")
    public List<Order> getOrders(
            @RequestParam(value = "platform_user_id", required = false) String platformUserId,
            @RequestParam(value = "status", required = false) String status,
            HttpServletRequest request) {

        Platform platform = (Platform) request.getAttribute("platform");

        return stateStore.getOrdersByPlatform(platform.getId()).stream()
                .filter(o -> platformUserId == null || platformUserId.equals(o.getPlatform_user_id()))
                .filter(o -> status == null || status.equals(o.getStatus()))
                .collect(Collectors.toList());
    }

    private void validateInstrument(String instrumentType, String instrumentId) {
        if ("STOCK".equals(instrumentType)) {
            if (stateStore.getStock(instrumentId).isEmpty()) {
                throw new ExchangeException(ErrorCode.INVALID_TICKER, "Stock not found: " + instrumentId);
            }
        } else if ("OPTION".equals(instrumentType)) {
            var option = stateStore.getOption(instrumentId)
                    .orElseThrow(() -> new ExchangeException(ErrorCode.INVALID_TICKER, "Option not found: " + instrumentId));
            if (!option.isIs_active()) {
                throw new ExchangeException(ErrorCode.OPTION_EXPIRED, "Option contract has expired: " + instrumentId);
            }
        } else {
            throw new ExchangeException(ErrorCode.INVALID_ORDER_TYPE, "instrument_type must be STOCK or OPTION.");
        }
    }
}
