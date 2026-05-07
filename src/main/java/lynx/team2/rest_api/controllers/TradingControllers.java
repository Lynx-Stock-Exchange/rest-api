package lynx.team2.rest_api.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lynx.team2.rest_api.LUtils;
import lynx.team2.rest_api.entities.MarketStateEntity;
import lynx.team2.rest_api.entities.OrderEntity;
import lynx.team2.rest_api.exceptions.ErrorCode;
import lynx.team2.rest_api.exceptions.ExchangeException;
import lynx.team2.rest_api.internal.Platform;
import lynx.team2.rest_api.models.Order;
import lynx.team2.rest_api.models.OrderRequest;
import lynx.team2.rest_api.repositories.MarketStateRepository;
import lynx.team2.rest_api.repositories.OptionContractRepository;
import lynx.team2.rest_api.repositories.OrderRepository;
import lynx.team2.rest_api.repositories.StockRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
public class TradingControllers {

    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final OptionContractRepository optionRepository;
    private final MarketStateRepository marketStateRepository;

    public TradingControllers(OrderRepository orderRepository,
                               StockRepository stockRepository,
                               OptionContractRepository optionRepository,
                               MarketStateRepository marketStateRepository) {
        this.orderRepository = orderRepository;
        this.stockRepository = stockRepository;
        this.optionRepository = optionRepository;
        this.marketStateRepository = marketStateRepository;
    }

    @PostMapping("")
    public Order postOrder(@RequestBody OrderRequest req, HttpServletRequest request) {
        Platform platform = (Platform) request.getAttribute("platform");

        // Market must be open
        boolean marketOpen = marketStateRepository.findById(1)
                .map(MarketStateEntity::getIsOpen)
                .orElse(false);
        if (!marketOpen) {
            throw new ExchangeException(ErrorCode.MARKET_CLOSED, "The market is currently closed.");
        }

        // Validate instrument
        validateInstrument(req.getInstrument_type(), req.getInstrument_id());

        // Validate order type
        if (!"MARKET".equals(req.getOrder_type()) && !"LIMIT".equals(req.getOrder_type())) {
            throw new ExchangeException(ErrorCode.INVALID_ORDER_TYPE, "order_type must be MARKET or LIMIT.");
        }

        // LIMIT orders require limit_price
        if ("LIMIT".equals(req.getOrder_type()) && req.getLimit_price() == null) {
            throw new ExchangeException(ErrorCode.INVALID_LIMIT_PRICE, "limit_price is required for LIMIT orders.");
        }

        // Validate side
        if (!"BUY".equals(req.getSide()) && !"SELL".equals(req.getSide())) {
            throw new ExchangeException(ErrorCode.INVALID_REQUEST, "side must be BUY or SELL.");
        }

        // Validate platform_user_id
        if (req.getPlatform_user_id() == null || req.getPlatform_user_id().isBlank()) {
            throw new ExchangeException(ErrorCode.INVALID_REQUEST, "platform_user_id is required.");
        }

        // Validate quantity
        if (req.getQuantity() == null || req.getQuantity() <= 0) {
            throw new ExchangeException(ErrorCode.INVALID_REQUEST, "quantity must be a positive integer.");
        }

        long now = Instant.now().getEpochSecond();
        Long expiresAt = null;
        if (req.getExpires_at() != null) {
            try {
                expiresAt = LUtils.isoToEpochSecond(req.getExpires_at());
            } catch (Exception e) {
                throw new ExchangeException(ErrorCode.INVALID_REQUEST, "expires_at must be ISO 8601 format, e.g. 2025-12-31T23:59:59");
            }
        }

        OrderEntity entity = new OrderEntity();
        entity.setOrderId(UUID.randomUUID().toString());
        entity.setPlatformId(platform.getId());
        entity.setPlatformUserId(req.getPlatform_user_id());
        entity.setInstrumentType(req.getInstrument_type());
        entity.setInstrumentId(req.getInstrument_id());
        entity.setOrderType(req.getOrder_type());
        entity.setSide(req.getSide());
        entity.setQuantity(req.getQuantity());
        entity.setLimitPrice(req.getLimit_price());
        entity.setStatus("PENDING");
        entity.setFilledQuantity(0);
        entity.setAverageFillPrice(null);
        entity.setExchangeFee(0.0);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setExpiresAt(expiresAt);

        orderRepository.save(entity);
        return toOrder(entity);
    }

    @GetMapping("/{order_id}")
    public Order getOrderById(@PathVariable("order_id") String orderId, HttpServletRequest request) {
        Platform platform = (Platform) request.getAttribute("platform");

        return orderRepository.findByOrderIdAndPlatformId(orderId, platform.getId())
                .map(this::toOrder)
                .orElseThrow(() -> new ExchangeException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderId));
    }

    @DeleteMapping("/{order_id}")
    public ResponseEntity<Order> deleteOrderById(@PathVariable("order_id") String orderId, HttpServletRequest request) {
        Platform platform = (Platform) request.getAttribute("platform");

        OrderEntity entity = orderRepository.findByOrderIdAndPlatformId(orderId, platform.getId())
                .orElseThrow(() -> new ExchangeException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + orderId));

        if (!"PENDING".equals(entity.getStatus()) && !"PARTIALLY_FILLED".equals(entity.getStatus())) {
            throw new ExchangeException(ErrorCode.ORDER_NOT_CANCELLABLE,
                    "Order cannot be cancelled in status: " + entity.getStatus());
        }

        entity.setStatus("CANCELLED");
        entity.setUpdatedAt(Instant.now().getEpochSecond());
        orderRepository.save(entity);

        return ResponseEntity.ok(toOrder(entity));
    }

    @GetMapping("")
    public List<Order> getOrders(
            @RequestParam(value = "platform_user_id", required = false) String platformUserId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "page_size", required = false, defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {

        Platform platform = (Platform) request.getAttribute("platform");

        Long fromEpoch = from != null ? LUtils.isoToEpochSecond(from) : null;
        Long toEpoch = to != null ? LUtils.isoToEpochSecond(to) : null;

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        return orderRepository.findByFilters(platform.getId(), platformUserId, status, fromEpoch, toEpoch, pageable)
                .getContent()
                .stream()
                .map(this::toOrder)
                .collect(Collectors.toList());
    }

    private void validateInstrument(String instrumentType, String instrumentId) {
        if ("STOCK".equals(instrumentType)) {
            if (stockRepository.findById(instrumentId).isEmpty()) {
                throw new ExchangeException(ErrorCode.INVALID_TICKER, "Stock not found: " + instrumentId);
            }
        } else if ("OPTION".equals(instrumentType)) {
            var option = optionRepository.findById(instrumentId)
                    .orElseThrow(() -> new ExchangeException(ErrorCode.INVALID_TICKER, "Option not found: " + instrumentId));
            if (!option.getIsActive()) {
                throw new ExchangeException(ErrorCode.OPTION_EXPIRED, "Option contract has expired: " + instrumentId);
            }
        } else {
            throw new ExchangeException(ErrorCode.INVALID_ORDER_TYPE, "instrument_type must be STOCK or OPTION.");
        }
    }

    private Order toOrder(OrderEntity e) {
        return new Order(
                e.getOrderId(),
                e.getPlatformId(),
                e.getPlatformUserId(),
                e.getInstrumentType(),
                e.getInstrumentId(),
                e.getOrderType(),
                e.getSide(),
                e.getQuantity(),
                e.getLimitPrice(),
                e.getStatus(),
                e.getFilledQuantity() != null ? e.getFilledQuantity() : 0,
                e.getAverageFillPrice(),
                e.getExchangeFee(),
                e.getCreatedAt() != null ? e.getCreatedAt() : 0L,
                e.getUpdatedAt() != null ? e.getUpdatedAt() : 0L,
                e.getExpiresAt() != null ? e.getExpiresAt() : 0L
        );
    }
}
