package com.calful.septatracker;

import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class VehiclesController {

    private final VehicleCacheService cache;

    public VehiclesController(VehicleCacheService cache) {
        this.cache = cache;
    }

    // Seed some example data into the cache/Redis
    @PostMapping("/vehicles/seed")
    public String seed(@RequestParam String route,
                       @RequestParam(defaultValue = "60") int ttlSeconds) {
        long now = Instant.now().getEpochSecond();
        var sample = List.of(
            new Vehicle("1234", route, 39.9526, -75.1652, 180.0, now),
            new Vehicle("9012", route, 39.9601, -75.1502, 270.0, now)
        );
        cache.putVehicles(route, sample, Duration.ofSeconds(ttlSeconds));
        return "Seeded " + sample.size() + " vehicles for route " + route +
               " with TTL " + ttlSeconds + " s";
    }

    // Read from the cache/Redis; if cache miss, return empty list for now
    @GetMapping("/vehicles")
    public List<Vehicle> vehicles(@RequestParam String route) {
        return cache.getVehicles(route).orElse(List.of());
    }

    // Simple DTO for /routes response
    public record RouteSummary(String routeId, int vehicles, Long ttlSeconds) {}

    @GetMapping("/routes")
    public List<RouteSummary> routes() {
        return cache.listRouteIds().stream()
                .map(r -> new RouteSummary(
                        r,
                        cache.countVehiclesForRoute(r),
                        cache.ttlSecondsForRoute(r)
                ))
                .toList();
    }
    

}
