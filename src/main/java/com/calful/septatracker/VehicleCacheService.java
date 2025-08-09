package com.calful.septatracker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

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
}
