package com.sv.svi.dao;

import com.sv.svi.utils.DownstreamMapper;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.data.tracks.GetSeveralTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import lombok.extern.slf4j.Slf4j;
import refinedDataModels.RefinedTrack;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
public class SpotifyTrackDao {
    private static final String collectionName = "RefinedTracks";
    private Integer retries = 0;

    public Future<Track> pullTrackAsync(SpotifyApi api, String id) {
        final GetTrackRequest request = api.getTrack(id).build();
        try {
            return request.executeAsync();
        } catch (Exception e) {
            log.error("Cannot download metadata for song with id {}, HTTP Status code: {}", id, e.getMessage());
            return null;
        }
    }

    public List<Track> getTracks(SpotifyApi api, List<String> ids) {
        List<Future<Track>> pulledTracksFutures = new ArrayList<>();

        if (ids.size() > 50) {
            Integer numBatches = (int) Math.ceil(ids.size() / 50.0);
            for (int i = 0; i < numBatches; i++) {
                try {
                    pulledTracksFutures.addAll(pullLimitedTrackFuture(api, ids.subList(i * 50, (i * 50) + 49)));
                } catch (Exception e) {
                    log.error("Cannot retrieve batch of songs from api, {}", e.getMessage());
                    return null;
                }
            }
        } else {
           pulledTracksFutures.addAll(pullLimitedTrackFuture(api, ids));
        }


        List<Track> completedTracks = new ArrayList<>();
        List<Future<Track>> retriedTracks = new ArrayList<>();

        while (pulledTracksFutures.size() > 0) {
            if (pulledTracksFutures.iterator().next().isDone()) {
                try {
                    Track track = pulledTracksFutures.iterator().next().get();
                    Integer index = pulledTracksFutures.indexOf(track);
                    completedTracks.add(track);
                    log.info("Successfully got track {}", track.getName());
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                } catch (ExecutionException e) {
                    log.error(e.getMessage() + ". Backing off will retry");
                    if (e.getCause() instanceof TooManyRequestsException) {
                       retriedTracks.add(pullTrackAsync(api, ids.get(index)));
                    }
                }
                pulledTracksFutures.iterator().remove();
            }
        }
        if (!retriedTracks.isEmpty()){
            retries++;
            log.info("{} tracks failed to be pulled. Retry number {}",  )
        }
        return completedTracks;
    }

    private List<Future<Track>> pullLimitedTrackFuture(SpotifyApi api, List<String> ids) {
        final GetSeveralTracksRequest request = api.getSeveralTracks(ids.toString()).build();
        try {
            return (List<Future<Track>>) request.executeAsync();
        } catch (Exception e) {
            log.error("Cannot retrieve batch of songs from api, {}", e.getMessage());
            return null;
        }
    }

    public List<RefinedTrack> pullAndMapTracks(SpotifyApi api, List<String> ids) throws MalformedURLException {
        if (ids.size() > 1) {
            List<Track> pulledTracks = (api, ids);
            List<RefinedTrack> mappedArtists = new ArrayList<>();
            for (Track track : pulledTracks) {
                mappedArtists.add(DownstreamMapper.mapTrack(track));
            }
            return mappedArtists;
        } else {
            return new ArrayList<>(Collections.singletonList(DownstreamMapper.mapTrack(pullTrack(api, ids.get(0)))));
        }

    }
}
