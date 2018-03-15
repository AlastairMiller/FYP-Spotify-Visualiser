package com.sv.svi.dto;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.requests.data.artists.GetArtistRequest;
import com.wrapper.spotify.requests.data.artists.GetSeveralArtistsRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ArtistDto {

    public static Artist pullArtist(SpotifyApi api, String id) {
        GetArtistRequest request = api.getArtist(id).build();
        try {
            return request.execute();
        } catch (Exception e) {
            log.error("Failed to download metadata for artist {}, HTTP error code: {} ", id, e.getMessage());
            return null;
        }

    }

    public static List<Artist> pullArtists(SpotifyApi api, List<String> ids) {
        List<Artist> pulledArtists = new ArrayList<>();
        if (ids.size() > 50) {
            final GetSeveralArtistsRequest request = api.getSeveralArtists(ids.subList(0, 49).toString()).build();
            ids = new ArrayList<>(ids.subList(49, ids.size()));
            try {
                pulledArtists.addAll(Arrays.asList(request.execute()));
            } catch (Exception e) {
                log.error("Cannot retrieve batch of Artists. HTTP error code: {}", e.getMessage());
            }
            pulledArtists.addAll(pullArtists(api, ids));
            return pulledArtists;
        } else {
            final GetSeveralArtistsRequest request = api.getSeveralArtists(ids.toString()).build();
            try {
                pulledArtists.addAll(Arrays.asList(request.execute()));
                return pulledArtists;
            } catch (Exception e) {
                log.error("Cannot retrieve batch of Artists. HTTP error code: {}", e.getMessage());
                return null;
            }
        }
    }
}
