package lynx.team2.rest_api.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.orders-requests}")
    private String ordersRequestsTopic;

    @Value("${kafka.topics.admin-commands}")
    private String adminCommandsTopic;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderRequest(Map<String, Object> order) {
        kafkaTemplate.send(ordersRequestsTopic, order);
    }

    public void sendAdminCommand(String action, Map<String, Object> payload) {
        kafkaTemplate.send(adminCommandsTopic, Map.of("action", action, "payload", payload));
    }
}
