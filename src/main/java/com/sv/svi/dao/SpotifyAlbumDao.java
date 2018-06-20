package com.sv.svi.dao;

import com.sv.svi.utils.DownstreamMapper;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.util.Arrays.asList;

@Slf4j
@Repository
public class SpotifyAlbumDao {
    private Integer retries = 0;

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
        if (ids.size() > 20) {
            final GetSeveralAlbumsRequest request = api.getSeveralAlbums(ids.subList(0, 19).toString()).build();
            ids = new ArrayList<>(ids.subList(19, ids.size()));
            try {
                pulledAlbums.addAll(asList(request.execute()));
            } catch (Exception e) {
                log.error("Cannot retrieve batch of Albums. HTTP error code: {}", e.getMessage());
            }
            pulledAlbums.addAll(pullAlbums(api, ids));
            return pulledAlbums;
        } else {
            final GetSeveralAlbumsRequest request = api.getSeveralAlbums(ids.toString()).build();
            try {
                pulledAlbums.addAll(asList(request.execute()));
                return pulledAlbums;
            } catch (Exception e) {
                log.error("Cannot retrieve batch of Albums. HTTP error code: {}", e.getMessage());
            }
            return pulledAlbums;
        }
    }

    public List<Album> pullAlbumsAsync(SpotifyApi api, List<String> albumIds) {
        List<Map<String, Future<Album[]>>> pulledAlbumFutures = new ArrayList<>();
        if (albumIds.size() > 20) {
            Integer numBatches = (int) Math.ceil(albumIds.size() / 20.0);
            Integer remainder = albumIds.size() % 20;
            for (int i = 0; i < numBatches; i++) {
                try {
                    if (i != numBatches - 1) {
                        pulledAlbumFutures.add(pullAlbumBatchFuture(api, albumIds.subList(i * 20, (i * 20) + 19)));
                    } else {
                        pulledAlbumFutures.add(pullAlbumBatchFuture(api, albumIds.subList(i * 20, (i * 20 + remainder))));
                    }
                } catch (Exception e) {
                    log.error("Cannot retrieve batch of albums from api, {}", e.getMessage());
                    return null;
                }
            }
        } else {
            pulledAlbumFutures.add(pullAlbumBatchFuture(api, albumIds));
        }
        return getAlbumBatchFutures(api, pulledAlbumFutures, albumIds);

    }


    private Map<String, Future<Album[]>> pullAlbumBatchFuture(SpotifyApi api, List<String> albumIds) {
        String idsAsString = albumIds.toString().replace("[", "").replace("]", "").replace(" ", "");
        final GetSeveralAlbumsRequest request = api.getSeveralAlbums(idsAsString).build();
        try {
            Future<Album[]> futureAlbums = request.executeAsync();
            Map<String, Future<Album[]>> futureAlbumBatch = new HashMap<>();
            futureAlbumBatch.put(albumIds.get(0), futureAlbums);
            return futureAlbumBatch;
        }catch (Exception e) {
            log.error("Cannot retrieve albums from api, {}", e.getMessage());
            return null;
        }

    }

    private List<Album> getAlbumBatchFutures(SpotifyApi api, List<Map<String,Future<Album[]>>> pulledAlbumFutures, List<String> albumIds) {
        List<Album> completedAlbums = new ArrayList<>();
        List<String> retriedAlbumIds = new ArrayList<>();

        while (pulledAlbumFutures.size() > 0) {
            Iterator<Map<String, Future<Album[]>>> iterator = pulledAlbumFutures.iterator();
            while (iterator.hasNext()) {

                Map<String, Future<Album[]>> nextFutureBatch = iterator.next();
                if (nextFutureBatch.values().iterator().next().isDone()) {
                    try {
                        return asList(nextFutureBatch.values().iterator().next().get());
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    } catch (ExecutionException e) {
                        log.error(e.getMessage() + ". Backing off will retry");
                        if (e.getCause() instanceof TooManyRequestsException) {
                            String key = nextFutureBatch.keySet().iterator().next();
                            retriedAlbumIds.add(key);
                        }
                    }
                    iterator.remove();
                }
            }
            if (!retriedAlbumIds.isEmpty()) {
                if (retries < 20) {
                    retries++;
                    log.info("{} artists failed to be pulled. Retry number {}", retriedAlbumIds.size(), retries);
                    pullAlbumsAsync(api, retriedAlbumIds);
                } else {
                    log.error("{} artists failed to be pulled and retries have been exceeded", retriedAlbumIds.size());
                }
            }
        }
        return completedAlbums;
    }

    public List<RefinedAlbum> pullAndMapAlbums(SpotifyApi api, List<String> ids) throws MalformedURLException {
        if (ids.size() > 1) {
            List<Album> pulledAlbums = pullAlbums(api, ids);
            List<RefinedAlbum> mappedAlbums = new ArrayList<>();
            for (Album album : pulledAlbums) {
                mappedAlbums.add(DownstreamMapper.mapAlbum(album));
            }
            return mappedAlbums;
        } else {
            return new ArrayList<>(Collections.singletonList(DownstreamMapper.mapAlbum(pullAlbum(api, ids.get(0)))));
        }
    }
}
