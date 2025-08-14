package com.calful.septatracker;

import com.google.transit.realtime.GtfsRealtime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GtfsPollingService {

    private final GtfsRealtimeClient client;
    private final VehicleCacheService cache;
    private final Duration ttl;

    private volatile long lastPollEpochSec = 0;
    private volatile int lastEntityCount = 0;
    private volatile int lastRouteCount = 0;

    private static final Logger log = LoggerFactory.getLogger(GtfsPollingService.class);

    public GtfsPollingService(GtfsRealtimeClient client,
                             VehicleCacheService cache,
                             @Value("${cache.ttl-seconds:60}") int ttlSeconds) {
        this.client = client;
        this.cache = cache;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    // Poll every feeds.poll-ms (default 15s)
    @Scheduled(fixedRateString = "${feeds.poll-ms:15000}")
    public void pollAndCache() {
        log.info("Starting GTFS poll...");
        try {
            var feed = client.fetch();
            log.info("Fetched feed with {} entities", feed.getEntityCount());

            // Group vehicles by routeId so we can cache "vehicles:route:{id}" lists
            Map<String, List<Vehicle>> byRoute = feed.getEntityList().stream()
                .filter(GtfsRealtime.FeedEntity::hasVehicle)
                .map(e -> e.getVehicle()) // VehiclePosition
                .map(vp -> {
                    var pos = vp.getPosition();
                    var ts = vp.hasTimestamp() ? vp.getTimestamp() : System.currentTimeMillis() / 1000;
                    String routeId = vp.getTrip().hasRouteId() ? vp.getTrip().getRouteId() : "unknown";
                    String vid = vp.hasVehicle() && vp.getVehicle().hasId() ? vp.getVehicle().getId() : UUID.randomUUID().toString();
                    Double bearing = pos.hasBearing() ? (double) pos.getBearing() : null;
                    return new Vehicle(vid, routeId, pos.getLatitude(), pos.getLongitude(), bearing, ts);
                })
                .collect(Collectors.groupingBy(Vehicle::routeId));

            log.info("Grouped {} vehicles into {} routes", 
                byRoute.values().stream().mapToInt(List::size).sum(), 
                byRoute.size());

            // Cache the vehicles by route
            byRoute.forEach((route, vehicles) -> {
                log.info("Caching {} vehicles for route {}", vehicles.size(), route);
                cache.putVehicles(route, vehicles, ttl);
            });
            
            lastPollEpochSec = System.currentTimeMillis() / 1000;
            lastEntityCount = feed.getEntityCount();
            lastRouteCount = byRoute.size();

            log.info("GTFS poll completed successfully");
        } catch (Exception e) {
            log.error("Error during GTFS poll", e);
        }
    }

    public long getLastPollEpochSec() {
        return lastPollEpochSec;
    }
    public int getLastEntityCount() {
        return lastEntityCount;    
    }
    public int getLastRouteCount() {
        return lastRouteCount;
    }

}
