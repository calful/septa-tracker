package com.calful.septatracker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
public class VehiclesController {

    @GetMapping("/vehicles")
    public List<Vehicle> vehicles(
            @RequestParam(name = "route", required = false) String routeId
    ) {
        // Fake data for now; pretend we fetched this from a cache/feed
        var now = Instant.now().getEpochSecond();
        var all = List.of(
            new Vehicle("1234", "10", 39.9526, -75.1652, 180.0, now),
            new Vehicle("5678", "34", 39.9489, -75.1733,  90.0, now),
            new Vehicle("9012", "10", 39.9601, -75.1502, 270.0, now)
        );

        if (routeId == null || routeId.isBlank()) return all;

        return all.stream()
                  .filter(v -> v.routeId().equals(routeId))
                  .toList();
    }
}
