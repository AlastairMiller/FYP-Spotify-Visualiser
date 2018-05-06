package com.sv.svi.dao;

import com.sv.svi.utils.DownstreamMapper;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.requests.data.albums.GetAlbumRequest;
import com.wrapper.spotify.requests.data.albums.GetSeveralAlbumsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import refinedDataModels.RefinedAlbum;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
public class SpotifyAlbumDao {

    public static Album pullAlbum(SpotifyApi api, String id) {
        final GetAlbumRequest request = api.getAlbum(id).build();
        try {
            return request.execute();
        } catch (SpotifyWebApiException | IOException e) {
            log.error("Failed to download metadata for album {}, HTTP error code: {} ", id, e.getMessage());
            return null;
        }
    }

    public static List<Album> pullAlbums(SpotifyApi api, List<String> ids) {
        List<Album> pulledAlbums = new ArrayList<>();
        if (ids.size() > 50) {
            final GetSeveralAlbumsRequest request = api.getSeveralAlbums(ids.subList(0, 49).toString()).build();
            ids = new ArrayList<>(ids.subList(49, ids.size()));
            try {
                pulledAlbums.addAll(Arrays.asList(request.execute()));
            } catch (Exception e) {
                log.error("Cannot retrieve batch of Albums. HTTP error code: {}", e.getMessage());
            }
            pulledAlbums.addAll(pullAlbums(api, ids));
            return pulledAlbums;
        } else {
            final GetSeveralAlbumsRequest request = api.getSeveralAlbums(ids.toString()).build();
            try {
                pulledAlbums.addAll(Arrays.asList(request.execute()));
                return pulledAlbums;
            } catch (Exception e) {
                log.error("Cannot retrieve batch of Albums. HTTP error code: {}", e.getMessage());
            }
            return pulledAlbums;
        }
    }

    public List<RefinedAlbum> pullAndMapAlbums(SpotifyApi api, List<String> ids) throws MalformedURLException {
        if (ids.size() > 1) {
            List<Album> pulledAlbums = pullAlbums(api, ids);
            List<RefinedAlbum> mappedAlbums = new ArrayList<>();
            for(Album album : pulledAlbums){
                mappedAlbums.add(DownstreamMapper.mapAlbum(album));
            }
            return mappedAlbums;
        } else {
            return new ArrayList<>(Collections.singletonList(DownstreamMapper.mapAlbum(pullAlbum(api, ids.get(0)))));
        }
    }
}
