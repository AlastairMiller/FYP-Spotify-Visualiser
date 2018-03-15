package com.sv.svi.dto;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.data.tracks.GetSeveralTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class TrackDto {

    public static Track pullSong(SpotifyApi api, String id) {
        final GetTrackRequest request = api.getTrack(id).build();
        try {
            return request.execute();
        } catch (Exception e) {
            log.error("Cannot download metadata for song with id {}, HTTP Status code: {}", id, e.getMessage());
            return null;
        }
    }

    public static List<Track> pullSongs(SpotifyApi api, ArrayList<String> ids) {
        List<Track> pulledTracks = new ArrayList<>();
        if (ids.size() > 50) {
            final GetSeveralTracksRequest request = api.getSeveralTracks(ids.subList(0, 49).toString()).build();
            ids = new ArrayList<>(ids.subList(49, ids.size()));
            try {
                pulledTracks.addAll(Arrays.asList(request.execute()));
            } catch (Exception e) {
                log.error("Cannot retrieve batch of songs from api, HTTP Status code: {}", e.getMessage());
                return null;
            }
            pulledTracks.addAll(pullSongs(api, ids));
            return pulledTracks;
        } else {
            final GetSeveralTracksRequest request = api.getSeveralTracks(ids.toString()).build();
            try {
                pulledTracks.addAll(Arrays.asList(request.execute()));
                return pulledTracks;
            } catch (Exception e) {
                log.error("Cannot retrieve batch of songs from api, HTTP error code: {}", e.getMessage());
                return null;
            }
        }
    }
}
