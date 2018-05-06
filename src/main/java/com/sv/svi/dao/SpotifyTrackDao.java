package com.sv.svi.dao;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.data.tracks.GetSeveralTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

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
        List<Map<String, Future<Track>>> pulledTracksFutures = new ArrayList<>();

        if (ids.size() > 50) {
            Integer numBatches = (int) Math.ceil(ids.size() / 50.0);
            for (int i = 0; i < numBatches; i++) {
                try {
                    pulledTracksFutures.addAll(pullTrackBatchFutures(api, ids.subList(i * 50, (i * 50) + 49)));
                } catch (Exception e) {
                    log.error("Cannot retrieve batch of songs from api, {}", e.getMessage());
                    return null;
                }
            }
        } else {
            pulledTracksFutures.addAll(pullTrackBatchFutures(api, ids));

        }
        return getTrackFutures(api, pulledTracksFutures);
    }

    private List<Map<String, Future<List<Track>>>> pullTrackBatchFutures(SpotifyApi api, List<String> ids) {
        final GetSeveralTracksRequest request = api.getSeveralTracks(ids.toString()).build();
        try {
            Future<List<Track>> futureList = request.executeAsync();
           // rewrite this
//            List<Map<String, Future<Track>>> listToReturn = new ArrayList();
//            for (int i = 0; i < ids.size(); i++) {
//                Map<String, Future<Track>> indexFuture = new HashMap<>();
//                indexFuture.put(ids.get(i), futureList.get(i));
//                listToReturn.add(indexFuture);
//            }
            return listToReturn;
        } catch (Exception e) {
            log.error("Cannot retrieve batch of songs from api, {}", e.getMessage());
            return null;
        }
    }

    public List<Track> getTrackFutures(SpotifyApi api, List<Map<String, Future<Track>>> pulledTrackFutures) {
        List<Track> completedTracks = new ArrayList<>();
        List<Map<String, Future<Track>>> retriedTracks = new ArrayList<>();

        while (completedTracks.size() > 0) {
            for (Iterator<Map<String, Future<Track>>> iterator = pulledTrackFutures.iterator(); iterator.hasNext(); ) {

                Map<String, Future<Track>> nextFutureMap = iterator.next();
                if (nextFutureMap.values().iterator().next().isDone()) {
                    try {
                        Track track = nextFutureMap.values().iterator().next().get();
                        completedTracks.add(track);
                        log.info("Successfully got track {}", track.getName());
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    } catch (ExecutionException e) {
                        log.error(e.getMessage() + ". Backing off will retry");
                        if (e.getCause() instanceof TooManyRequestsException) {
                            String key = nextFutureMap.keySet().iterator().next();
                            retriedTracks.add(pullTrackAsync(api, key));
                        }
                    }
                    iterator.remove();
                }
            }
            if (!retriedTracks.isEmpty()) {
                if (retries < 20) {
                    retries++;
                    log.info("{} tracks failed to be pulled. Retry number {}", retriedTracks.size(), retries);
                    getTrackFutures(api, retriedTracks);
                } else {
                    log.error("{} tracks failed to be pulled and retries have been exceeded", retriedTracks.size());
                }
            }
        }
        return completedTracks;
    }

}
