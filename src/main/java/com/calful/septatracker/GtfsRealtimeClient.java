package com.calful.septatracker;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class GtfsRealtimeClient {
    
    private static final Logger log = LoggerFactory.getLogger(GtfsRealtimeClient.class);
    
    private final HttpClient http = HttpClient.newHttpClient();
    private final URI feedUri;

    public GtfsRealtimeClient(@Value("${feeds.vehiclePositionsUrl}") String feedUrl) {
        this.feedUri = URI.create(feedUrl);
        log.info("GTFS client configured for URL: {}", feedUrl);
    }

    public FeedMessage fetch() {
        try {
            log.debug("Fetching GTFS feed from {}", feedUri);
            var req = HttpRequest.newBuilder(feedUri).GET().build();
            var res = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (res.statusCode() / 100 != 2) {
                throw new RuntimeException("Bad response from GTFS feed: " + res.statusCode());
            }
            var feed = FeedMessage.parseFrom(res.body());
            log.debug("Successfully parsed GTFS feed with {} entities", feed.getEntityCount());
            return feed;
        } catch (Exception e) {
            log.error("Failed to fetch/parse GTFS feed from {}", feedUri, e);
            throw new RuntimeException("Failed to fetch/parse GTFS feed", e);
        }
    }
}
