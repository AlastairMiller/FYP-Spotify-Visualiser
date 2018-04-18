package com.sv.svi.dto;

import com.sv.svi.utils.DownstreamMapper;
import com.wrapper.spotify.SpotifyApi;
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

@Slf4j
public class TrackDto {

    public static Track pullTrack(SpotifyApi api, String id) {
        final GetTrackRequest request = api.getTrack(id).build();
        try {
            return request.execute();
        } catch (Exception e) {
            log.error("Cannot download metadata for song with id {}, HTTP Status code: {}", id, e.getMessage());
            return null;
        }
    }

    public static List<Track> pullTracks(SpotifyApi api, List<String> ids) {
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
            pulledTracks.addAll(pullTracks(api, ids));
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

    public List<RefinedTrack> pullAndMapTracks(SpotifyApi api, List<String> ids) throws MalformedURLException {
        if (ids.size() > 1) {
            List<Track> pulledTracks = pullTracks(api, ids);
            List<RefinedTrack> mappedArtists = new ArrayList<>();
            for(Track track : pulledTracks){
                mappedArtists.add(DownstreamMapper.mapTrack(track));
            }
            return mappedArtists;
        } else {
            return new ArrayList<>(Collections.singletonList(DownstreamMapper.mapTrack(pullTrack(api, ids.get(0)))));
        }

    }
}
