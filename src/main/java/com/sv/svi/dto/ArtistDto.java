package com.sv.svi.dto;

import com.sv.svi.utils.DownstreamMapper;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.requests.data.artists.GetArtistRequest;
import com.wrapper.spotify.requests.data.artists.GetSeveralArtistsRequest;
import lombok.extern.slf4j.Slf4j;
import refinedDataModels.RefinedArtist;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ArtistDto {

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

    public List<RefinedArtist> pullAndMapArtists(SpotifyApi api, List<String> ids) throws MalformedURLException {
        if (ids.size() > 1) {
            List<Artist> pulledArtists = pullArtists(api, ids);
            List<RefinedArtist> mappedArtists = new ArrayList<>();
            for(Artist artist : pulledArtists){
                mappedArtists.add(DownstreamMapper.mapArtist(artist));
            }
            return mappedArtists;
        } else {
            return new ArrayList<>(Collections.singletonList(DownstreamMapper.mapArtist(pullArtist(api, ids.get(0)))));
        }

    }
}
