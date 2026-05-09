package lynx.team2.rest_api.controllers;

import lynx.team2.rest_api.LUtils;
import lynx.team2.rest_api.entities.*;
import lynx.team2.rest_api.models.*;
import lynx.team2.rest_api.repositories.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin("*") // remove this when deployed
public class AdminController {

    @Value("${kafka.topics.admin-commands}")
    private String adminCommandsTopic;

    @Value("${market.events.url}")
    private String marketEventsUrl;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestClient restClient = RestClient.create();
    private final StockRepository stockRepository;
    private final OptionContractRepository optionRepository;
    private final MarketStateRepository marketStateRepository;
    private final MarketEventRepository marketEventRepository;
    private final FeeConfigRepository feeConfigRepository;
    private final TradeRepository tradeRepository;
    private final PlatformRepository platformRepository;
    private final OrderRepository orderRepository;

    public AdminController(KafkaTemplate<String, Object> kafkaTemplate,
                           StockRepository stockRepository,
                           OptionContractRepository optionRepository,
                           MarketStateRepository marketStateRepository,
                           MarketEventRepository marketEventRepository,
                           FeeConfigRepository feeConfigRepository,
                           TradeRepository tradeRepository,
                           PlatformRepository platformRepository,
                           OrderRepository orderRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.stockRepository = stockRepository;
        this.optionRepository = optionRepository;
        this.marketStateRepository = marketStateRepository;
        this.marketEventRepository = marketEventRepository;
        this.feeConfigRepository = feeConfigRepository;
        this.tradeRepository = tradeRepository;
        this.platformRepository = platformRepository;
        this.orderRepository = orderRepository;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void publish(String action, Map<String, Object> payload) {
        kafkaTemplate.send(adminCommandsTopic, new AdminCommand(action, payload));
    }

    private static class AdminCommand {
        private final String action;
        private final Map<String, Object> payload;

        AdminCommand(String action, Map<String, Object> payload) {
            this.action = action;
            this.payload = payload;
        }

        public String getAction() { return action; }
        public Map<String, Object> getPayload() { return payload; }
    }

    private MarketStateEntity getOrCreateMarketState() {
        return marketStateRepository.findById(1).orElseGet(() -> {
            MarketStateEntity s = new MarketStateEntity();
            s.setIsOpen(false);
            s.setSpeedMultiplier(1);
            return marketStateRepository.save(s);
        });
    }

    private FeeConfigEntity getOrCreateFeeConfig() {
        return feeConfigRepository.findById(1).orElseGet(() -> {
            FeeConfigEntity f = new FeeConfigEntity();
            return feeConfigRepository.save(f);
        });
    }

    private Stock toStockModel(StockEntity e) {
        return new Stock(
                e.getTicker(), e.getName(), e.getSector(),
                e.getCurrentPrice() != null ? e.getCurrentPrice() : 0.0,
                e.getOpenPrice() != null ? e.getOpenPrice() : 0.0,
                e.getHighPrice() != null ? e.getHighPrice() : 0.0,
                e.getLowPrice() != null ? e.getLowPrice() : 0.0,
                e.getVolume() != null ? e.getVolume() : 0,
                e.getVolatility() != null ? e.getVolatility() : 0.0,
                e.getTrendBias() != null ? e.getTrendBias() : 0.0,
                e.getEventWeight() != null ? e.getEventWeight() : 0.0,
                e.getMomentum() != null ? e.getMomentum() : 0.0,
                e.getListedAt() != null ? e.getListedAt() : 0L
        );
    }

    private StockEntity toStockEntity(StockSeedRequest req) {
        StockEntity e = new StockEntity();
        e.setTicker(req.getTicker());
        e.setName(req.getName());
        e.setSector(req.getSector());
        e.setCurrentPrice(req.getStart_price());
        e.setOpenPrice(req.getStart_price());
        e.setHighPrice(req.getStart_price());
        e.setLowPrice(req.getStart_price());
        e.setVolume(0);
        e.setVolatility(req.getVolatility());
        e.setTrendBias(req.getTrend_bias());
        e.setEventWeight(req.getEvent_weight());
        e.setMomentum(req.getMomentum());
        e.setListedAt(Instant.now().getEpochSecond());
        return e;
    }

    private OptionContract toOptionModel(OptionContractEntity e) {
        return new OptionContract(
                e.getOptionId(), e.getUnderlyingTicker(), e.getOptionType(),
                e.getStrikePrice() != null ? e.getStrikePrice() : 0.0,
                e.getExpiryTime() != null ? e.getExpiryTime() : 0L,
                e.getPremium() != null ? e.getPremium() : 0.0,
                e.getIsActive() != null && e.getIsActive(),
                e.getAutoExercise() != null && e.getAutoExercise()
        );
    }

    private MarketEvent toMarketEventModel(MarketEventEntity e) {
        return new MarketEvent(
                e.getEventId(), e.getEventType(), e.getScope(), e.getTarget(),
                e.getMagnitude() != null ? e.getMagnitude() : 0.0,
                e.getDurationTicks() != null ? e.getDurationTicks() : 0,
                e.getHeadline(),
                e.getTriggeredAt() != null ? e.getTriggeredAt() : 0L,
                e.getTriggeredBy()
        );
    }


    // -------------------------------------------------------------------------
    // PLATFORMS
    // -------------------------------------------------------------------------

    @PostMapping("/platforms")
    public ResponseEntity<Map<String, Object>> postPlatform(@RequestBody PlatformCreateRequest req) {
        PlatformEntity p = new PlatformEntity();
        p.setId(UUID.randomUUID().toString());
        p.setName(req.getName());
        p.setApiKey(UUID.randomUUID().toString().replace("-", ""));
        p.setApiSecret(UUID.randomUUID().toString().replace("-", ""));
        platformRepository.save(p);
        publish("PLATFORM_ADDED", Map.of("id", p.getId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", p.getId(),
                "name", p.getName(),
                "api_key", p.getApiKey(),
                "api_secret", p.getApiSecret()
        ));
    }

    @DeleteMapping("/platforms/{id}")
    public ResponseEntity<Void> deletePlatform(@PathVariable String id) {
        if (!platformRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<OrderEntity> activeOrders = orderRepository.findActiveByPlatformId(id);
        activeOrders.forEach(o -> o.setStatus("CANCELLED"));
        orderRepository.saveAll(activeOrders);
        platformRepository.deleteById(id);
        publish("PLATFORM_REMOVED", Map.of("id", id));
        return ResponseEntity.noContent().build();
    }


    // -------------------------------------------------------------------------
    // MARKET
    // -------------------------------------------------------------------------

    @GetMapping("/market/status")
    public ResponseEntity<Map<String, Object>> getMarketStatus() {
        MarketStateEntity state = getOrCreateMarketState();
        Map<String, Object> status = new java.util.HashMap<>();
        status.put("is_open", state.getIsOpen());
        long now = Instant.now().getEpochSecond();
        status.put("market_time", LUtils.longToIsoDate(state.getMarketTime() != null ? state.getMarketTime() : now));
        status.put("real_time", LUtils.longToIsoDate(state.getRealTime() != null ? state.getRealTime() : now));
        status.put("speed_multiplier", state.getSpeedMultiplier());
        status.put("active_event", state.getActiveEventId());
        return ResponseEntity.ok(status);
    }

    @PostMapping("/market/open")
    public ResponseEntity<Void> postOpen() {
        MarketStateEntity state = getOrCreateMarketState();
        state.setIsOpen(true);
        state.setRealTime(Instant.now().getEpochSecond());
        marketStateRepository.save(state);
        publish("OPEN_MARKET", Map.of());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/market/close")
    public ResponseEntity<Void> postClose() {
        MarketStateEntity state = getOrCreateMarketState();
        state.setIsOpen(false);
        marketStateRepository.save(state);
        publish("CLOSE_MARKET", Map.of());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PutMapping("/market/speed")
    public ResponseEntity<Void> putSpeed(@RequestBody SpeedUpdateRequest request) {
        MarketStateEntity state = getOrCreateMarketState();
        state.setSpeedMultiplier(request.getMultiplier());
        marketStateRepository.save(state);
        publish("MARKET_SPEED_UPDATE", Map.of("multiplier", request.getMultiplier()));
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }


    // -------------------------------------------------------------------------
    // STOCKS
    // -------------------------------------------------------------------------

    @GetMapping("/stocks")
    public ResponseEntity<List<Stock>> getAllStocks() {
        return ResponseEntity.ok(stockRepository.findAll().stream().map(this::toStockModel).toList());
    }

    @GetMapping("/stocks/{ticker}")
    public ResponseEntity<Stock> getStock(@PathVariable String ticker) {
        return stockRepository.findById(ticker)
                .map(e -> ResponseEntity.ok(toStockModel(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/stocks")
    public ResponseEntity<Stock> postSingleStock(@RequestBody StockSeedRequest req) {
        if (stockRepository.existsById(req.getTicker())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        StockEntity saved = stockRepository.save(toStockEntity(req));
        publish("STOCK_ADDED", Map.of("ticker", saved.getTicker()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toStockModel(saved));
    }

    @PutMapping("/stocks/{ticker}")
    public ResponseEntity<Stock> updateStock(@PathVariable String ticker, @RequestBody StockUpdateRequest req) {
        return stockRepository.findById(ticker)
                .map(e -> {
                    if (req.getName() != null) e.setName(req.getName());
                    if (req.getSector() != null) e.setSector(req.getSector());
                    if (req.getVolatility() != null) e.setVolatility(req.getVolatility());
                    if (req.getTrend_bias() != null) e.setTrendBias(req.getTrend_bias());
                    if (req.getEvent_weight() != null) e.setEventWeight(req.getEvent_weight());
                    if (req.getMomentum() != null) e.setMomentum(req.getMomentum());
                    StockEntity saved = stockRepository.save(e);
                    publish("STOCK_UPDATED", Map.of("ticker", ticker));
                    return ResponseEntity.ok(toStockModel(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/stocks/{ticker}")
    public ResponseEntity<Void> deleteStock(@PathVariable String ticker) {
        if (!stockRepository.existsById(ticker)) {
            return ResponseEntity.notFound().build();
        }
        stockRepository.deleteById(ticker);
        publish("STOCK_REMOVED", Map.of("ticker", ticker));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/stocks/seed")
    public ResponseEntity<List<Stock>> seedStocks(@RequestBody List<StockSeedRequest> stocks) {
        List<StockEntity> toSave = stocks.stream()
                .filter(req -> !stockRepository.existsById(req.getTicker()))
                .map(this::toStockEntity)
                .toList();
        List<Stock> saved = stockRepository.saveAll(toSave).stream().map(this::toStockModel).toList();
        if (!saved.isEmpty()) {
            publish("STOCKS_SEEDED", Map.of("count", saved.size()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }


    // -------------------------------------------------------------------------
    // OPTIONS
    // -------------------------------------------------------------------------

    @GetMapping("/options")
    public ResponseEntity<List<OptionContract>> getAllOptions() {
        return ResponseEntity.ok(optionRepository.findAll().stream().map(this::toOptionModel).toList());
    }

    @GetMapping("/options/{optionId}")
    public ResponseEntity<OptionContract> getOption(@PathVariable String optionId) {
        return optionRepository.findById(optionId)
                .map(e -> ResponseEntity.ok(toOptionModel(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/options")
    public ResponseEntity<OptionContract> postOption(@RequestBody OptionCreateAdminRequest req) {
        if (!stockRepository.existsById(req.getUnderlying_ticker())) {
            return ResponseEntity.badRequest().build();
        }
        OptionContractEntity e = new OptionContractEntity();
        e.setOptionId(UUID.randomUUID().toString());
        e.setUnderlyingTicker(req.getUnderlying_ticker());
        e.setOptionType(req.getOption_type());
        e.setStrikePrice(req.getStrike_price());
        e.setExpiryTime(LUtils.isoToEpochSecond(req.getExpiry_time()));
        e.setPremium(req.getInitial_premium());
        e.setIsActive(true);
        e.setAutoExercise(true);
        OptionContractEntity saved = optionRepository.save(e);
        publish("OPTION_ADDED", Map.of("option_id", saved.getOptionId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toOptionModel(saved));
    }

    @PutMapping("/options/{optionId}")
    public ResponseEntity<OptionContract> updateOption(@PathVariable String optionId, @RequestBody OptionUpdateRequest req) {
        return optionRepository.findById(optionId)
                .map(e -> {
                    if (req.getStrike_price() != null) e.setStrikePrice(req.getStrike_price());
                    if (req.getExpiry_time() != null) e.setExpiryTime(LUtils.isoToEpochSecond(req.getExpiry_time()));
                    if (req.getPremium() != null) e.setPremium(req.getPremium());
                    if (req.getIs_active() != null) e.setIsActive(req.getIs_active());
                    OptionContractEntity saved = optionRepository.save(e);
                    publish("OPTION_UPDATED", Map.of("option_id", optionId));
                    return ResponseEntity.ok(toOptionModel(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/options/{optionId}")
    public ResponseEntity<Void> deleteOption(@PathVariable String optionId) {
        if (!optionRepository.existsById(optionId)) {
            return ResponseEntity.notFound().build();
        }
        optionRepository.deleteById(optionId);
        publish("OPTION_REMOVED", Map.of("option_id", optionId));
        return ResponseEntity.noContent().build();
    }


    // -------------------------------------------------------------------------
    // EVENTS
    // -------------------------------------------------------------------------

    @GetMapping("/events")
    public ResponseEntity<List<MarketEvent>> getEvents() {
        return ResponseEntity.ok(marketEventRepository.findRecentEvents().stream()
                .map(this::toMarketEventModel).toList());
    }

    @PostMapping("/events/trigger")
    public ResponseEntity<MarketEvent> postEvent(@RequestBody MarketEventTriggerRequest req) {
        Map<String, Object> forwardBody = new java.util.HashMap<>();
        forwardBody.put("event_type", req.getEvent_type());
        forwardBody.put("scope", req.getScope());
        forwardBody.put("target", req.getTarget());
        forwardBody.put("magnitude", req.getMagnitude());
        forwardBody.put("duration_ticks", req.getDuration_ticks());

        Map<String, Object> response;
        try {
            response = restClient.post()
                    .uri(marketEventsUrl + "/api/v1/market/events/trigger")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(forwardBody)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<>() {});
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        if (response == null) return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();

        MarketEventEntity e = new MarketEventEntity();
        e.setEventId((String) response.get("event_id"));
        e.setEventType(String.valueOf(response.get("event_type")));
        e.setScope(String.valueOf(response.get("scope")));
        e.setTarget((String) response.get("target"));
        e.setMagnitude(response.get("magnitude") instanceof Number n ? n.doubleValue() : null);
        e.setDurationTicks(response.get("duration_ticks") instanceof Number n ? n.intValue() : null);
        e.setHeadline((String) response.get("headline"));
        e.setTriggeredAt(Instant.now().getEpochSecond());
        e.setTriggeredBy("ADMIN");
        MarketEventEntity saved = marketEventRepository.save(e);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(toMarketEventModel(saved));
    }


    // -------------------------------------------------------------------------
    // FEES
    // -------------------------------------------------------------------------

    @GetMapping("/fees")
    public ResponseEntity<Map<String, Object>> getFees() {
        return ResponseEntity.ok(Map.of("fee_rate", getOrCreateFeeConfig().getFeeRate()));
    }

    @PutMapping("/fees")
    public ResponseEntity<Map<String, Object>> putFees(@RequestBody Map<String, Object> feeUpdate) {
        FeeConfigEntity config = getOrCreateFeeConfig();
        if (feeUpdate.containsKey("fee_rate")) {
            double rate = ((Number) feeUpdate.get("fee_rate")).doubleValue();
            if (rate < 0) return ResponseEntity.badRequest().build();
            config.setFeeRate(rate);
            feeConfigRepository.save(config);
            publish("FEE_UPDATED", Map.of("fee_rate", rate));
        }
        return ResponseEntity.ok(Map.of("fee_rate", config.getFeeRate()));
    }


    // -------------------------------------------------------------------------
    // REVENUE
    // -------------------------------------------------------------------------

    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenue() {
        Double total = tradeRepository.sumExchangeFees();
        return ResponseEntity.ok(Map.of("total_revenue", total != null ? total : 0.0));
    }
}