package lynx.team2.rest_api.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lynx.team2.rest_api.services.MarketStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MarketEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(MarketEventConsumer.class);

    private final MarketStateService marketStateService;
    private final ObjectMapper objectMapper;

    public MarketEventConsumer(MarketStateService marketStateService, ObjectMapper objectMapper) {
        this.marketStateService = marketStateService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "market.events", groupId = "rest-api-market-events")
    public void onMarketEvent(String rawPayload) {
        try {
            JsonNode json = objectMapper.readTree(rawPayload);
            String eventType = json.path("event_type").asText("");
            switch (eventType) {
                case "MARKET_OPEN"  -> { marketStateService.setOpen(true);  log.info("Market state synced: OPEN");   }
                case "MARKET_CLOSE" -> { marketStateService.setOpen(false); log.info("Market state synced: CLOSED"); }
            }
        } catch (Exception e) {
            log.debug("Skipped non-status market event: {}", e.getMessage());
        }
    }
}
