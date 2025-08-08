# Philly SEPTA Real-Time Tracker – Design Doc

## Overview

This service ingests real-time vehicle positions and trip updates from SEPTA’s GTFS-Realtime feed, parses and caches the data, and exposes transit info via REST API endpoints for public use.

## Goals
- Fast access to live SEPTA vehicle data
- Scalable backend with observability and infra-as-code

## Architecture

**Data Flow:**
1. Poll SEPTA’s GTFS-Realtime feed (protobuf over HTTP)
2. Parse feed into domain objects (Java)
3. Cache objects in Redis (TTL ~60s per vehicle)
4. Expose REST endpoint `/vehicles?route={id}` to serve cached data

**(Future)**
- WebSocket/SSE updates
- ETA calculations
- Minimal React/Leaflet frontend

## Tech Choices

- **Backend:** Java + Spring Boot
- **Feed parsing:** Protobuf → Java (Maven/Gradle)
- **Cache:** Redis
- **Infra:** Docker, AWS Fargate, ElastiCache, Terraform
- **Observability:** OpenTelemetry, CloudWatch/Grafana

## Milestones

1. Repo/init, README & design doc
2. Setup Spring Boot project
3. Integrate GTFS-Realtime parser
4. Redis caching layer
5. REST API for vehicles
6. Deployment pipeline & infra

## Open Questions

- How frequently to poll SEPTA’s feed?
- What’s the best schema for caching vehicles in Redis?
- Public/open API or require authentication?
- How to handle error/retry logic for upstream feed failures?

## Future Feature Ideas
- Tracker Map/Board
- "How much time do I have to leave?" meter

## References

- [SEPTA GTFS-Realtime Documentation](http://www3.septa.org/developer/)
- [GTFS-Realtime Spec](https://developers.google.com/transit/gtfs-realtime/)

