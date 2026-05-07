package lynx.team2.rest_api.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lynx.team2.rest_api.entities.TradeEntity;
import lynx.team2.rest_api.internal.Platform;
import lynx.team2.rest_api.models.PortfolioPosition;
import lynx.team2.rest_api.models.Trade;
import lynx.team2.rest_api.repositories.TradeRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {

    private final TradeRepository tradeRepository;

    public PortfolioController(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @GetMapping("/{platform_user_id}")
    public List<PortfolioPosition> getPortfolioById(
            @PathVariable("platform_user_id") String platformUserId,
            HttpServletRequest request) {

        Platform platform = (Platform) request.getAttribute("platform");
        List<TradeEntity> trades = tradeRepository.findByPlatformIdAndPlatformUserId(platform.getId(), platformUserId);

        // Group by (instrument_type, instrument_id) and aggregate
        Map<String, int[]> netQuantity = new LinkedHashMap<>();      // key → [totalBuyQty, totalSellQty]
        Map<String, double[]> costBasis = new LinkedHashMap<>();     // key → [totalBuyCost]
        Map<String, String[]> instrumentMeta = new LinkedHashMap<>(); // key → [type, id]

        for (TradeEntity t : trades) {
            String key = t.getInstrumentType() + ":" + t.getInstrumentId();
            netQuantity.computeIfAbsent(key, k -> new int[]{0, 0});
            costBasis.computeIfAbsent(key, k -> new double[]{0.0});
            instrumentMeta.put(key, new String[]{t.getInstrumentType(), t.getInstrumentId()});

            if ("BUY".equals(t.getSide())) {
                netQuantity.get(key)[0] += t.getQuantity();
                costBasis.get(key)[0] += t.getQuantity() * (t.getPrice() != null ? t.getPrice() : 0.0);
            } else {
                netQuantity.get(key)[1] += t.getQuantity();
            }
        }

        List<PortfolioPosition> positions = new ArrayList<>();
        for (String key : netQuantity.keySet()) {
            int[] qty = netQuantity.get(key);
            int net = qty[0] - qty[1];
            if (net <= 0) continue; // no position to report

            double totalBuyCost = costBasis.get(key)[0];
            double avgCost = qty[0] > 0 ? totalBuyCost / qty[0] : 0.0;
            String[] meta = instrumentMeta.get(key);

            positions.add(new PortfolioPosition(
                    platform.getId(),
                    platformUserId,
                    meta[0],
                    meta[1],
                    net,
                    avgCost
            ));
        }
        return positions;
    }

    @GetMapping("/{platform_user_id}/trades")
    public List<Trade> getTradesById(
            @PathVariable("platform_user_id") String platformUserId,
            HttpServletRequest request) {

        Platform platform = (Platform) request.getAttribute("platform");

        return tradeRepository.findByPlatformIdAndPlatformUserId(platform.getId(), platformUserId)
                .stream()
                .map(t -> new Trade(
                        t.getTradeId(),
                        t.getOrderId(),
                        t.getPlatformId(),
                        t.getPlatformUserId(),
                        t.getInstrumentType(),
                        t.getInstrumentId(),
                        t.getSide(),
                        t.getQuantity() != null ? t.getQuantity() : 0,
                        t.getPrice() != null ? t.getPrice() : 0.0,
                        t.getExchangeFee() != null ? t.getExchangeFee() : 0.0,
                        t.getExecutedAt() != null ? t.getExecutedAt() : 0L
                ))
                .collect(Collectors.toList());
    }
}
