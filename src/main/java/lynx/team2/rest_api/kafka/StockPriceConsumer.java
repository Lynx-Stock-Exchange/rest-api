package lynx.team2.rest_api.kafka;

import lynx.team2.rest_api.repositories.StockRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StockPriceConsumer {

    private final StockRepository stockRepository;

    public StockPriceConsumer(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @KafkaListener(topics = "${kafka.topics.stock-prices}", groupId = "rest-api")
    public void consume(Map<String, Object> message) {
        Object payloadObj = message.get("payload");
        if (!(payloadObj instanceof Map<?, ?> raw)) return;

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) raw;

        String ticker = (String) payload.get("ticker");
        if (ticker == null) return;

        stockRepository.findById(ticker).ifPresent(stock -> {
            if (payload.get("price") instanceof Number n) stock.setCurrentPrice(n.doubleValue());
            if (payload.get("open") instanceof Number n) stock.setOpenPrice(n.doubleValue());
            if (payload.get("high") instanceof Number n) stock.setHighPrice(n.doubleValue());
            if (payload.get("low") instanceof Number n) stock.setLowPrice(n.doubleValue());
            if (payload.get("volume") instanceof Number n) stock.setVolume(n.intValue());
            stockRepository.save(stock);
        });
    }
}
