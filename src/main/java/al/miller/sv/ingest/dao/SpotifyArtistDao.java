package al.miller.sv.ingest.dao;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.requests.data.artists.GetArtistRequest;
import com.wrapper.spotify.requests.data.artists.GetSeveralArtistsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.util.Arrays.asList;

@Slf4j
@Repository
public class SpotifyArtistDao {
    private Integer retries = 0;

    public static Artist pullArtist(SpotifyApi api, String id) {
        final GetArtistRequest request = api.getArtist(id).build();
        try {
            return request.execute();
        } catch (Exception e) {
            log.error("Failed to download metadata for artist {}, HTTP error code: {} ", id, e.getMessage());
            return null;
        }
    }

    public Future<Artist> pullArtistAsync(SpotifyApi api, String id) {
        final GetArtistRequest request = api.getArtist(id).build();
        try {
            return request.executeAsync();
        } catch (Exception e) {
            log.error("Failed to download metadata for artist {}, HTTP error code: {} ", id, e.getMessage());
            return null;
        }
    }

    public List<Artist> pullArtistsAsync(SpotifyApi api, List<String> ids) throws InterruptedException {
        List<Map<String, Future<Artist[]>>> pulledArtistFutures = new ArrayList<>();

        if (ids.size() > 50) {
            Integer numBatches = (int) Math.ceil(ids.size() / 50.0);
            Integer remainder = ids.size() % 50;
            for (int i = 0; i < numBatches; i++) {
                try {
                    if (i != numBatches - 1) {
                        pulledArtistFutures.add(pullArtistBatchFutures(api, ids.subList(i * 50, (i * 50) + 49)));
                    } else {
                        pulledArtistFutures.add(pullArtistBatchFutures(api, ids.subList(i * 50, (i * 50 + remainder))));
                    }
                } catch (Exception e) {
                    log.error("Cannot retrieve batch of artists from api, {}", e.getMessage());
                    return null;
                }
            }
        } else {
            pulledArtistFutures.add(pullArtistBatchFutures(api, ids));
        }
        return getArtistBatchFutures(api, pulledArtistFutures);
    }

    public Map<String, Future<Artist[]>> pullArtistBatchFutures(SpotifyApi api, List<String> artistIds) {
        String idsAsString = artistIds.toString();
        idsAsString = idsAsString.replace("[", "").replace("]", "").replace(" ", "");
        final GetSeveralArtistsRequest request = api.getSeveralArtists(idsAsString).build();
        try {
            Future<Artist[]> futureList = request.executeAsync();
            Map<String, Future<Artist[]>> futureBatch = new HashMap<>();
            futureBatch.put(artistIds.get(0), futureList);
            return futureBatch;
        } catch (Exception e) {
            log.error("Cannot submit artist batch to api", e.getMessage());
            return null;
        }
    }

    public List<Artist> getArtistBatchFutures(SpotifyApi api, List<Map<String, Future<Artist[]>>> pulledArtistFutures) throws InterruptedException {
        List<Artist> completedArtists = new ArrayList<>();
        List<String> retriedArtistIds = new ArrayList<>();
        Boolean apiLimitHit = false;

        while (pulledArtistFutures.size() > 0) {
            Iterator<Map<String, Future<Artist[]>>> iterator = pulledArtistFutures.iterator();
            while (iterator.hasNext()) {

                Map<String, Future<Artist[]>> nextFutureBatch = iterator.next();
                if (nextFutureBatch.values().iterator().next().isDone()) {
                    try {
                        return asList(nextFutureBatch.values().iterator().next().get());
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    } catch (ExecutionException e) {
                        log.error(e.getMessage() + ". Backing off will retry");
                        if (e.getCause() instanceof TooManyRequestsException) {
                            if (!apiLimitHit){
                                apiLimitHit = true;
                                log.error("Artist API rate limit exceeded. Will wait {} seconds and retry", ((TooManyRequestsException) e.getCause()).getRetryAfter());
                                Thread.sleep(((TooManyRequestsException) e.getCause()).getRetryAfter() * 1000);
                            }else {
                                apiLimitHit= false;
                            }
                            String key = nextFutureBatch.keySet().iterator().next();
                            retriedArtistIds.add(key);
                        }
                    }
                    iterator.remove();
                }
            }
            if (!retriedArtistIds.isEmpty()) {
                if (retries < 20) {
                    retries++;
                    log.info("{} artists failed to be pulled. Retry number {}", retriedArtistIds.size(), retries);
                    pullArtistsAsync(api, retriedArtistIds);
                } else {
                    log.error("{} artists failed to be pulled and retries have been exceeded", retriedArtistIds.size());
                }
            }
        }
        return completedArtists;
    }


}
