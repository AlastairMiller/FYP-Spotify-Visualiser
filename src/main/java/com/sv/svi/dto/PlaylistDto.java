package com.sv.svi.dto;

import com.mongodb.client.MongoCollection;
import com.sv.svi.utils.DownstreamMapper;
import com.svd.dao.MongoDao;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.requests.data.playlists.GetListOfUsersPlaylistsRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import refinedDataModels.RefinedPlaylist;
import refinedDataModels.RefinedTrack;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
@NoArgsConstructor
public class PlaylistDto {
    private static final String collectionName = "RefinedPlaylists";

    public List<PlaylistSimplified> pullPlaylistsForAUser(SpotifyApi api, String userId, int offset) {
        final GetListOfUsersPlaylistsRequest request = api.getListOfUsersPlaylists(userId).limit(50).offset(offset).build();
        try {
            List<PlaylistSimplified> usersPlaylist = new ArrayList<>(Arrays.asList(request.execute().getItems()));
            log.info("Pulled {} simple playlists for user {}", offset, userId);
            if (usersPlaylist.size() == 50) {
                usersPlaylist.addAll(pullPlaylistsForAUser(api, userId, offset + 50));
            }
            return usersPlaylist;
        } catch (Exception e) {
            log.error("Failed to grab user's: {} playlist", userId);
            return null;
        }
    }


    public void saveSimplePlaylistsToMongo(SpotifyApi api, List<PlaylistSimplified> playlistsToSave, MongoDao playlistDao, ExecutorService executor) {
        List<Future> futureList = new ArrayList<>();
        for (PlaylistSimplified simplePlaylist : playlistsToSave) {
            Future<Object> completableFuture = convertSimplePlaylistToPlaylist(api, simplePlaylist);
            futureList.add(completableFuture);
        }
        log.info("Pulling {} playlists", futureList.size());
        List<Playlist> completedPlaylists = new ArrayList<>();
        while (futureList.size() > 0) {
            for (Iterator<Future> it = futureList.iterator(); it.hasNext(); ) {
                Future completableFuture = it.next();
                if (completableFuture.isDone()) {
                    Object object = null;
                    try {
                        object = completableFuture.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        return;
                    }
                    completedPlaylists.add((Playlist) object);
                    it.remove();
                }
            }
        }
        log.info("Pulled {} playlists", completedPlaylists.size());
        savePlaylists(api, completedPlaylists, playlistDao);
    }

    public static Playlist pullPlaylist(SpotifyApi api, String ownerId, String playlistId) {
        final GetPlaylistRequest request = api.getPlaylist(ownerId, playlistId).build();
        try {
            return request.execute();
        } catch (Exception e) {
            log.error("Failed to download metadata for playlist {}, HTTP error code: {}", playlistId, e.getMessage());
            return null;
        }

    }

    private static Future<Object> pullPlaylistAsync(SpotifyApi api, String ownerId, String playlistId) {
        final GetPlaylistRequest request = api.getPlaylist(ownerId, playlistId).build();
        try {
            return request.executeAsync();
        } catch (Exception e) {
            log.error("Failed to download metadata for playlist {}, HTTP error code: {}", playlistId, e.getMessage());
            return null;
        }
    }


    private Future<Object> convertSimplePlaylistToPlaylist(SpotifyApi api, PlaylistSimplified simplePlaylist) {
        return pullPlaylistAsync(api, simplePlaylist.getOwner().getId(), simplePlaylist.getId());
    }


    public void savePlaylists(SpotifyApi api, List<Playlist> playlists, MongoDao playlistDao) throws MalformedURLException {
        MongoCollection mongoCollection = playlistDao.getCollection(collectionName);
        for (Playlist playlist : playlists) {
            if (playlistDao.retrieveEntryById(playlist.getId(), collectionName) == null) {
                try {
                    playlistDao.saveEntryToDatabase(RefinedPlaylist.class, DownstreamMapper.mapPlaylist(playlist), mongoCollection);
                    log.info("Saved a playlist called {}", playlist.getName());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            } else {
               playlistCleanup(api, DownstreamMapper.mapPlaylist(playlist), playlistDao);
            }
        }
    }


    private void playlistCleanup(SpotifyApi api, RefinedPlaylist newRefinedPlaylist, MongoDao playlistDao) {
        List<String> storedTracks = (List<String>) playlistDao.retrieveEntryById(newRefinedPlaylist.getId(), collectionName).get("");
        for (int i = 0; i < storedTracks.size(); i++) {
            Boolean remove = true;
            for (int p = 0; p < newRefinedPlaylist.getTrackIds().size(); p++) {
                if (storedTracks.get(i).equals(newRefinedPlaylist.getTrackIds().get(p))) {
                    remove = false;
                    p = newRefinedPlaylist.getTrackIds().size() - 1;
                }
            }
            if (remove) {
                log.info("Removing song: {} from playlist: {}", storedTracks.get(i), newRefinedPlaylist.getName());
                storedTracks.remove(i);
            }
        }
        playlistDao.deleteEntryinDb();
        playlistDao.saveEntryToDatabase(RefinedPlaylist.class, newRefinedPlaylist, playlistDao.getCollection(collectionName));
    }
}
