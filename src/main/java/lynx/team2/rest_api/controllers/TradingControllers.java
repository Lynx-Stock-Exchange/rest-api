package lynx.team2.rest_api.controllers;

import lynx.team2.rest_api.models.Order;
import lynx.team2.rest_api.models.Stock;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class TradingControllers {

    /**
     * POST /orders <br>
     * Submit a new order. Must include platform credentials <br>
     * TODO: Replace with actual function
     * @return The created {@link Order} object
     */
    @PostMapping("")
    public Order postOrder() {
        return Order.getDummy("order-abc-123");
    }

    /**
     * GET /orders/:order_id <br>
     * TODO: Replace with actual data
     * @return The current state of an order as {@link Order}. Platforms can only retrieve their own orders
     */
    @GetMapping("/{order_id}")
    public Order getOrderById(@PathVariable("order_id") String order_id) {
        return Order.getDummy(order_id);
    }

    /**
     * DELETE /orders/:order_id <br>
     * Cancels a {@code PENDING} or {@code PARTIALLY_FILLED} order. Transitions status to {@code CANCELLED} <br>
     * TODO: Replace with actual function
     */
    @DeleteMapping("/{order_id}")
    public void deleteOrderById(@PathVariable("order_id") String order_id) {

    }

    /**
     * GET /orders <br>
     * TODO: Replace with actual data <br>
     * Query params:<br>
     *     platform_user_id <br>
     *     status <br>
     *     from <br>
     *     to <br>
     *     page <br>
     *     page_size
     * @return Paginated order history for the platform
     */
    @GetMapping("")
    public List<Order> getOrders(
            @RequestParam(value = "platform_user_id", required = false) String platform_user_id,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page_size", required = false) Integer page_size
    ) {
        List<Order> orders = new ArrayList<>();
        orders.add(new Order(
                "order-abc-123",
                "platform-abc-123",
                platform_user_id,
                "STOCK",
                "ARKA",
                "LIMIT",
                "BUY",
                (page != null) ? page : 50,
                128.0,
                (status != null) ? status : "FILLED",
                50,
                12.34,
                1.23,
                1710511200L,
                1710511200L,
                1710511200L
        ));
        return orders;
    }
}
