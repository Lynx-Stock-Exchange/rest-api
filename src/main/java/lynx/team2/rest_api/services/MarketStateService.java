package lynx.team2.rest_api.services;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MarketStateService {

    private final AtomicBoolean marketOpen = new AtomicBoolean(true);
    private final AtomicInteger speedMultiplier = new AtomicInteger(60);

    public boolean isOpen() { return marketOpen.get(); }
    public void setOpen(boolean open) { marketOpen.set(open); }

    public int getSpeedMultiplier() { return speedMultiplier.get(); }
    public void setSpeedMultiplier(int multiplier) { speedMultiplier.set(multiplier); }
}
