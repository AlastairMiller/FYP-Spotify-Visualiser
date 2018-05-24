package com.sv.svi.dao;

import com.sv.svi.utils.DownstreamMapper;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.requests.data.artists.GetArtistRequest;
import com.wrapper.spotify.requests.data.artists.GetSeveralArtistsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import refinedDataModels.RefinedArtist;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

@Slf4j
@Repository
public class SpotifyArtistDao {

     public Future<Artist> pullArtistAsync(SpotifyApi api, String id) {
        final GetArtistRequest request = api.getArtist(id).build();
        try {
            return request.executeAsync();
        } catch (Exception e) {
            log.error("Failed to download metadata for artist {}, HTTP error code: {} ", id, e.getMessage());
            return null;
        }
    }

    public static Artist pullArtist(SpotifyApi api, String id) {
         final GetArtistRequest request = api.getArtist(id).build();
         try {
             return request.execute();
         } catch (Exception e) {
             log.error("Failed to download metadata for artist {}, HTTP error code: {} ", id, e.getMessage());
             return null;
         }
    }

    private List<Artist> pullArtists(SpotifyApi api, List<String> ids) {
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
            }
            return pulledArtists;
        }
    }

    public List<Artist> pullArtistsAsync(SpotifyApi api, ArrayList<String> artistIdsToPull) {
    }
}
