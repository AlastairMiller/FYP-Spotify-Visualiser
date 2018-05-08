package com.sv.svi.dao;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.data.tracks.GetSeveralTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Repository
public class SpotifyTrackDao {
    private Integer retries = 0;

    public static Track pullTrack(SpotifyApi api, String id) {
        final GetTrackRequest request = api.getTrack(id).build();
        try {
            return request.execute();
        } catch (Exception e) {
            log.error("Cannot download metadata for song with id {}, HTTP Status code: {}", id, e.getMessage());
            return null;
        }
    }

    private Map<String, Future<Track>> pullTrackAsync(SpotifyApi api, String id) {
        final GetTrackRequest request = api.getTrack(id).build();
        try {
            Map<String, Future<Track>> map = new HashMap<>();
            map.put(id, request.executeAsync());
            return map;
        } catch (Exception e) {
            log.error("Cannot download metadata for song with id {}, HTTP Status code: {}", id, e.getMessage());
            return null;
        }
    }

    public List<Track> pullTracksAsync(SpotifyApi api, List<String> ids) {
        List<Map<String, Future<List<Track>>>> pulledTracksFutures = new ArrayList<>();

        if (ids.size() > 50) {
            Integer numBatches = (int) Math.ceil(ids.size() / 50.0);
            for (int i = 0; i < numBatches; i++) {
                try {
                    pulledTracksFutures.add(pullTrackBatchFutures(api, ids.subList(i * 50, (i * 50) + 49)));
                } catch (Exception e) {
                    log.error("Cannot retrieve batch of songs from api, {}", e.getMessage());
                    return null;
                }
            }
        } else {
            pulledTracksFutures.add(pullTrackBatchFutures(api, ids));

        }
        return getTrackBatchFutures(api, pulledTracksFutures, ids);
    }

    private Map<String, Future<List<Track>>> pullTrackBatchFutures(SpotifyApi api, List<String> ids) {
        String idsAsString = ids.toString();
        idsAsString = idsAsString.replace("[", "").replace("]", "").replace(" ", "");
        final GetSeveralTracksRequest request = api.getSeveralTracks(idsAsString).build();
        try {
            Future<List<Track>> futureList = request.executeAsync();
            Map<String, Future<List<Track>>> futureBatch = new HashMap<>();
            futureBatch.put(ids.get(0), futureList);
            return futureBatch;
        } catch (Exception e) {
            log.error("Cannot retrieve batch of songs from api, {}", e.getMessage());
            return null;
        }
    }

    public List<Track> getTrackBatchFutures(SpotifyApi api, List<Map<String, Future<List<Track>>>> pulledTrackFutures, List<String> ids) {
        List<Track> completedTracks = new ArrayList<>();
        List<String> retriedTrackIds = new ArrayList<>();

        while (pulledTrackFutures.size() > 0) {
            Iterator<Map<String, Future<List<Track>>>> iterator = pulledTrackFutures.iterator();
            while (iterator.hasNext()) {

                Map<String, Future<List<Track>>> nextFutureBatch = iterator.next();
                if (nextFutureBatch.values().iterator().next().isDone()) {
                    try {
                        // Broken asf
                        List<Track> trackList = new ArrayList<>(nextFutureBatch.values().iterator().next().get());
                        return null;
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    } catch (ExecutionException e) {
                        log.error(e.getMessage() + ". Backing off will retry");
                        if (e.getCause() instanceof TooManyRequestsException) {
                            String key = nextFutureBatch.keySet().iterator().next();
                            retriedTrackIds.add(key);
                        }
                    }
                    iterator.remove();
                }
            }

            if (!retriedTrackIds.isEmpty()) {
                if (retries < 20) {
                    retries++;
                    log.info("{} tracks failed to be pulled. Retry number {}", retriedTrackIds.size(), retries);
                    pullTracksAsync(api, retriedTrackIds);
                } else {
                    log.error("{} tracks failed to be pulled and retries have been exceeded", retriedTrackIds.size());
                }
            }
        }
        return completedTracks;
    }
}
