package lynx.team2.rest_api.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lynx.team2.rest_api.models.*;
import lynx.team2.rest_api.internal.Platform;
import lynx.team2.rest_api.state.StateStore;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaEventConsumer {

    private final StateStore stateStore;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KafkaEventConsumer(StateStore stateStore) {
        this.stateStore = stateStore;
    }

    @KafkaListener(topics = {"${kafka.topics.stock-prices}", "stock_prices", "price.update"}, groupId = "rest-api")
    public void consumeStockPrice(Map<String, Object> message) {
        JsonNode node = objectMapper.valueToTree(message);
        JsonNode payload = node.has("payload") ? node.get("payload") : node;

        String ticker = payload.path("ticker").asText(null);
        if (ticker == null) return;

        Stock stock = stateStore.getStock(ticker).orElse(new Stock());
        stock.setTicker(ticker);

        if (payload.has("price")) stock.setCurrent_price(payload.get("price").asDouble());
        if (payload.has("open")) stock.setOpen_price(payload.get("open").asDouble());
        if (payload.has("high")) stock.setHigh_price(payload.get("high").asDouble());
        if (payload.has("low")) stock.setLow_price(payload.get("low").asDouble());
        if (payload.has("volume")) stock.setVolume(payload.get("volume").asInt());

        stateStore.updateStock(stock);

        // Cache OHLC point for history endpoint
        if (stock.getCurrent_price() > 0) {
            long ts = payload.has("timestamp") ? payload.get("timestamp").asLong()
                    : payload.has("market_time") ? payload.get("market_time").asLong()
                    : System.currentTimeMillis() / 1000L;
            OHLCPoint point = new OHLCPoint(
                    stock.getCurrent_price(),
                    stock.getOpen_price(),
                    stock.getHigh_price(),
                    stock.getLow_price(),
                    stock.getVolume(),
                    ts
            );
            stateStore.addOHLCPoint(ticker, point);
        }
    }

    @KafkaListener(topics = {"${kafka.topics.orders-updates}", "order.updates", "order_updates"}, groupId = "rest-api")
    public void consumeOrderUpdate(Map<String, Object> message) {
        try {
            String type = (String) message.get("type");
            JsonNode payloadNode = objectMapper.valueToTree(message.get("payload"));

            if ("TRADE_EXECUTED".equals(type)) {
                Trade trade = objectMapper.treeToValue(payloadNode, Trade.class);
                if (trade.getTrade_id() != null) {
                    if (trade.getPlatform_user_id() == null) {
                        stateStore.getOrder(trade.getOrder_id()).ifPresent(order -> {
                            trade.setPlatform_user_id(order.getPlatform_user_id());
                            trade.setInstrument_type(order.getInstrument_type());
                        });
                    }
                    stateStore.addTrade(trade);
                    // Accumulate exchange revenue
                    if (trade.getExchange_fee() > 0) {
                        stateStore.addRevenue(trade.getExchange_fee());
                    }
                }
            } else if ("ORDER_UPDATE".equals(type)) {
                Order orderUpdate = objectMapper.treeToValue(payloadNode, Order.class);
                if (orderUpdate.getOrder_id() != null) {
                    stateStore.getOrder(orderUpdate.getOrder_id()).ifPresentOrElse(
                        existing -> {
                            if (orderUpdate.getStatus() != null) existing.setStatus(orderUpdate.getStatus());
                            if (orderUpdate.getFilled_quantity() != null) existing.setFilled_quantity(orderUpdate.getFilled_quantity());
                            if (orderUpdate.getAverage_fill_price() != null) existing.setAverage_fill_price(orderUpdate.getAverage_fill_price());
                            if (orderUpdate.getExchange_fee() != null) existing.setExchange_fee(orderUpdate.getExchange_fee());
                            stateStore.updateOrder(existing);
                        },
                        () -> stateStore.updateOrder(orderUpdate)
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing Order/Trade Update: " + e.getMessage());
        }
    }

    @KafkaListener(topics = {"${kafka.topics.market-events}", "market.events", "market_events"}, groupId = "rest-api")
    public void consumeMarketEvent(Map<String, Object> message) {
        try {
            JsonNode node = objectMapper.valueToTree(message);
            JsonNode payload = node.has("payload") ? node.get("payload") : node;
            MarketEvent event = objectMapper.treeToValue(payload, MarketEvent.class);
            if (event.getEvent_id() != null) {
                stateStore.addMarketEvent(event);
                stateStore.updateActiveEvent(event.getEvent_id());
            }
        } catch (Exception e) {
            System.err.println("Error parsing Market Event: " + e.getMessage());
        }
    }

    @KafkaListener(topics = {"market.ticks", "market_ticks"}, groupId = "rest-api")
    public void consumeMarketTick(Map<String, Object> message) {
        try {
            JsonNode node = objectMapper.valueToTree(message);
            JsonNode payload = node.has("payload") ? node.get("payload") : node;

            stateStore.updateMarketOpen(payload.path("is_open").asBoolean(false));

            if (payload.has("speed_multiplier")) {
                stateStore.updateMarketSpeed(payload.get("speed_multiplier").asInt());
            }
            if (payload.has("market_time")) {
                stateStore.updateMarketTime(payload.get("market_time").asLong());
            }
            if (payload.has("real_time")) {
                stateStore.updateRealTime(payload.get("real_time").asLong());
            }
        } catch (Exception e) {
            System.err.println("Error parsing Market Tick: " + e.getMessage());
        }
    }

    @KafkaListener(topics = {"${kafka.topics.admin-commands}", "admin.commands", "admin_commands"}, groupId = "rest-api")
    public void consumeAdminCommand(Map<String, Object> message) {
        String action = (String) message.get("action");
        Object payloadObj = message.get("payload");
        if (action == null || payloadObj == null) return;

        JsonNode payload = objectMapper.valueToTree(payloadObj);

        switch (action) {
            case "STOCK_ADDED":
            case "STOCK_UPDATED":
                try {
                    Stock stock = objectMapper.treeToValue(payload, Stock.class);
                    if (stock.getTicker() != null) {
                        stateStore.updateStock(stock);
                    }
                } catch (Exception e) {
                    String ticker = payload.path("ticker").asText(null);
                    if (ticker != null) {
                        Stock s = stateStore.getStock(ticker).orElse(new Stock());
                        s.setTicker(ticker);
                        stateStore.updateStock(s);
                    }
                }
                break;
            case "STOCK_REMOVED":
                String removedTicker = payload.path("ticker").asText(null);
                if (removedTicker != null) stateStore.removeStock(removedTicker);
                break;
            case "OPTION_ADDED":
            case "OPTION_UPDATED":
                try {
                    OptionContract option = objectMapper.treeToValue(payload, OptionContract.class);
                    if (option.getOption_id() != null) {
                        stateStore.updateOption(option);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing Option from admin command: " + e.getMessage());
                }
                break;
            case "PLATFORM_ADDED":
                try {
                    Platform platform = new Platform(
                        payload.path("id").asText(),
                        payload.path("name").asText()
                    );
                    if (platform.getId() != null && !platform.getId().isEmpty()) {
                        stateStore.updatePlatform(platform);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing Platform from admin command: " + e.getMessage());
                }
                break;
            case "PLATFORM_REMOVED":
                String platformId = payload.path("id").asText(null);
                if (platformId != null) stateStore.removePlatform(platformId);
                break;
            case "OPEN_MARKET":
                stateStore.updateMarketOpen(true);
                break;
            case "CLOSE_MARKET":
                stateStore.updateMarketOpen(false);
                break;
            case "MARKET_SPEED_UPDATE":
                if (payload.has("multiplier")) {
                    stateStore.updateMarketSpeed(payload.get("multiplier").asInt());
                }
                break;
            case "FEE_UPDATED":
            case "UPDATE_FEE":
                if (payload.has("fee_rate")) {
                    stateStore.setFeeRate(payload.get("fee_rate").asDouble());
                }
                break;
        }
    }
}