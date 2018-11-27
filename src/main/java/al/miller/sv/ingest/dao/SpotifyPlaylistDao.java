package al.miller.sv.ingest.dao;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.requests.data.playlists.GetListOfUsersPlaylistsRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import refinedDataModels.RefinedPlaylist;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@NoArgsConstructor
@Repository
public class SpotifyPlaylistDao {
    private static final String collectionName = "RefinedPlaylists";
    private Integer retries = 0;

    public List<PlaylistSimplified> pullPlaylistsForAUser(SpotifyApi api, String userId, int offset) {
        List<PlaylistSimplified> usersPlaylist = new ArrayList<>();
        final GetListOfUsersPlaylistsRequest request = api.getListOfUsersPlaylists(userId).limit(50).offset(offset).build();
        try {
            usersPlaylist = new ArrayList<>(Arrays.asList(request.execute().getItems()));
            log.info("Pulled {} simple playlists for user {}", offset, userId);
            if (usersPlaylist.size() == 50) {
                usersPlaylist.addAll(pullPlaylistsForAUser(api, userId, offset + 50));
            }
            return usersPlaylist;
        } catch (Exception e) {
            log.error("Failed to grab user's: {} playlist. Error details {}", userId, e.getMessage());
            return usersPlaylist;
        }
    }


    public List<Playlist> getPlaylistsFromSimplePlaylists(SpotifyApi api, List<PlaylistSimplified> playlistsToSave) throws InterruptedException {
        List<Map<String, Future<Playlist>>> futureList = new ArrayList<>();
        for (PlaylistSimplified simplePlaylistId : playlistsToSave) {
            Map<String, Future<Playlist>> completableFuture = convertSimplePlaylistToPlaylist(api, simplePlaylistId.getId(), simplePlaylistId.getOwner().getId());
            futureList.add(completableFuture);
        }
        log.info("Pulling {} playlists", futureList.size());

        return getPlaylists(api, futureList);
    }

    private List<Playlist> getPlaylists(SpotifyApi api, List<Map<String, Future<Playlist>>> futureList) throws InterruptedException {
        List<Playlist> completedPlaylists = new ArrayList<>();
        List<Map<String, Future<Playlist>>> retriedPlaylists = new ArrayList<>();
        Boolean apiLimitHit = false;

        while (futureList.size() > 0) {
            //Iterator there as removing completed playlists (Even if rate limited) V messy tho :(((((
            for (Iterator<Map<String, Future<Playlist>>> iterator = futureList.iterator(); iterator.hasNext(); ) {
                Map<String, Future<Playlist>> stringFutureMap = iterator.next();
                if (stringFutureMap.values().iterator().next().isDone()) {
                    try {
                        Playlist playlist = stringFutureMap.values().iterator().next().get();
                        completedPlaylists.add(playlist);
                        log.info("Successfully got playlist {}", playlist.getName());
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    } catch (ExecutionException e) {
                        if (e.getCause() instanceof TooManyRequestsException) {
                            if (!apiLimitHit) {
                                apiLimitHit = true;
                                log.error("Playlist API rate limit exceeded. Will wait {} seconds and retry", ((TooManyRequestsException) e.getCause()).getRetryAfter());
                                Thread.sleep(((TooManyRequestsException) e.getCause()).getRetryAfter() * 1000);
                            } else {
                                apiLimitHit = false;
                            }
                            String[] key = stringFutureMap.keySet().iterator().next().split("_");
                            retriedPlaylists.add(pullPlaylistAsync(api, key[0], key[1]));
                        }
                    }
                    iterator.remove();
                }
            }
        }
        if (!retriedPlaylists.isEmpty()) {
            retries++;
            log.info("{} playlists failed to be pulled. Retry number {}", retriedPlaylists.size(), retries);
            if (retries < 20) {
                completedPlaylists.addAll(getPlaylists(api, retriedPlaylists));
            } else {
                log.error("Ditching {} playlists as retries exceeded {}", retriedPlaylists.size(), retries);
            }
        }
        return completedPlaylists;
    }

    public static Playlist pullPlaylistSync(SpotifyApi api, String ownerId, String playlistId) throws InterruptedException {
        final GetPlaylistRequest request = api.getPlaylist(ownerId, playlistId).build();
        try {
            return request.execute();
        } catch (Exception e) {
            if (e instanceof TooManyRequestsException) {
                log.error("Playlist API rate limit exceeded. Will wait {} seconds and retry", ((TooManyRequestsException) e.getCause()).getRetryAfter());
                Thread.sleep(((TooManyRequestsException) e).getRetryAfter() * 1000);
                return pullPlaylistSync(api, ownerId, playlistId);
            }
            log.error("Failed to download metadata for playlist {}, HTTP error code: {}", playlistId, e.getMessage());
            return null;
        }
    }


    private static Map<String, Future<Playlist>> pullPlaylistAsync(SpotifyApi api, String ownerId, String playlistId) {
        final GetPlaylistRequest request = api.getPlaylist(ownerId, playlistId).build();
        try {
            Map<String, Future<Playlist>> map = new HashMap<>();
            map.put(ownerId + "_" + playlistId, request.executeAsync());
            return map;
        } catch (Exception e) {
            log.error("Failed to download metadata for playlist {}, HTTP error code: {}", playlistId, e.getMessage());
            return null;
        }
    }


    private Map<String, Future<Playlist>> convertSimplePlaylistToPlaylist(SpotifyApi api, String simplePlaylistId, String ownerName) {
        return pullPlaylistAsync(api, ownerName, simplePlaylistId);
    }
}
