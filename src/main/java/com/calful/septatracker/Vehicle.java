package com.calful.septatracker;

// Java 17+ record = immutable DTO; Spring/Jackson serializes it to JSON automatically
public record Vehicle(
    String id,
    String routeId,
    double lat,
    double lon,
    Double bearing,     // nullable
    long epochSeconds
) {}
