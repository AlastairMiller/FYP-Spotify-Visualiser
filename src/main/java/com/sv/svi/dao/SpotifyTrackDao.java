package com.sv.svi.dao;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.data.tracks.GetSeveralTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
public class SpotifyTrackDao {
    private Integer retries = 0;

    private Future<Track> pullTrackAsync(SpotifyApi api, String id) {
        final GetTrackRequest request = api.getTrack(id).build();
        try {
            return request.executeAsync();
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
                    pulledTracksFutures.addAll(pull50TrackFutures(api, ids.subList(i * 50, (i * 50) + 49)));
                } catch (Exception e) {
                    log.error("Cannot retrieve batch of songs from api, {}", e.getMessage());
                    return null;
                }
            }
        } else {
            pulledTracksFutures.addAll(pull50TrackFutures(api, ids));
        }
        return getTrackFutures(api, pulledTracksFutures);
    }

    private List<Map<String, Future<Track>>> pull50TrackFutures(SpotifyApi api, List<String> ids) {
        final GetSeveralTracksRequest request = api.getSeveralTracks(ids.toString()).build();
        try {
            List<Future<Track>> futureList = (List<Future<Track>>) request.executeAsync();
            List<Map<String, Future<Track>>> listToReturn = new ArrayList();
            for (int i = 0; i < ids.size(); i++) {
                Map<String, Future<Track>> indexFuture = new HashMap<>();
                indexFuture.put(ids.get(i), futureList.get(i));
                listToReturn.add(indexFuture);
            }
            return listToReturn;
        } catch (Exception e) {
            log.error("Cannot retrieve batch of songs from api, {}", e.getMessage());
            return null;
        }
    }

    public List<Track> getTrackFutures(SpotifyApi api, List<Map<String, Future<Track>>> pulledTrackFutures) {
        List<Track> completedTracks = new ArrayList<>();
        List<Map<String, Future<Track>>> retriedTracks = new ArrayList<>();

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
                        String[] key = nextFutureMap.keySet().iterator().next().split("_");
                        retriedTracks.add(new HashMap<>( pullTrackAsync(api, key[0])));
                    }
                }
                iterator.remove();
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
