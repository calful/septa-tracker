package com.calful.septatracker;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private final GtfsPollingService polling;

    public HealthController(GtfsPollingService polling) {
        this.polling = polling;
    }
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "ok",
            "lastPollEpochSec", polling.getLastPollEpochSec(),
            "lastPollIso", Instant.ofEpochSecond(polling.getLastPollEpochSec()).toString(),
            "lastEntityCount", polling.getLastEntityCount(),
            "lastRouteCount", polling.getLastRouteCount()
        );
    }
}
