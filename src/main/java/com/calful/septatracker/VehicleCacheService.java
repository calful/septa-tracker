package com.calful.septatracker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@Service
public class VehicleCacheService {
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final ValueOperations<String, String> ops;

    public VehicleCacheService(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
        this.ops = redis.opsForValue();
    }

    private static String keyForRoute(String routeId) {
        return "vehicles:route:" + routeId;
    }

    public void putVehicles(String routeId, List<Vehicle> vehicles, Duration ttl) {
        try {
            String json = mapper.writeValueAsString(vehicles);
            ops.set(keyForRoute(routeId), json, ttl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize vehicles to JSON", e);
        }
    }

    public Optional<List<Vehicle>> getVehicles(String routeId) {
        String json = ops.get(keyForRoute(routeId));
        if (json == null) return Optional.empty();
        try {
            return Optional.of(
                mapper.readValue(json, new TypeReference<List<Vehicle>>() {})
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize vehicles from JSON", e);
        }
    }

    private static final String ROUTE_KEY_PREFIX = "vehicles:route:";

    // List all routeIds currently cached (debug use)
    public Set<String> listRouteIds() {
        // keys command(redis.keys) is not recommended for production use, but ok for locally debugging
        var keys = redis.keys(ROUTE_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return Set.of();
        var routes = new TreeSet<String>(); // sorted for stable output
        for (String k : keys) {
            routes.add(k.substring(ROUTE_KEY_PREFIX.length()));
        }
        return routes;
    }

    // Remaining TTL for a route key (in seconds)
    public Long ttlSecondsForRoute(String routeId) {
        // Use ops bound connection for TTL since StringRedisTemplate lacks a direct helper
        var conn = redis.getRequiredConnectionFactory().getConnection();
        try {
            return conn.keyCommands().ttl((ROUTE_KEY_PREFIX + routeId).getBytes());
        } finally {
            conn.close();
        }
    }

    // Count cached vehicles for a route (0 if miss)
    public int countVehiclesForRoute(String routeId) {
        return getVehicles(routeId).map(List::size).orElse(0);
    }
}
