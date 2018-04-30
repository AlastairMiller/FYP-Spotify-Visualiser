package com.sv.svi.dao;

import com.mongodb.client.MongoCollection;
import com.sv.svi.utils.DownstreamMapper;
import com.svd.dao.MongoDao;
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

import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@NoArgsConstructor
@Repository
public class SpotifyPlaylistDao {
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


    public List<Playlist> getPlaylistsFromSimplePlaylists(SpotifyApi api, List<PlaylistSimplified> playlistsToSave) throws MalformedURLException {
        List<Map<String, Future<Playlist>>> futureList = new ArrayList<>();
        for (PlaylistSimplified simplePlaylist : playlistsToSave) {
            Map<String, Future<Playlist>> completableFuture = convertSimplePlaylistToPlaylist(api, simplePlaylist);
            futureList.add(completableFuture);
        }
        log.info("Pulling {} playlists", futureList.size());

        return getPlaylists(api, futureList);
    }

    private List<Playlist> getPlaylists(SpotifyApi api, List<Map<String, Future<Playlist>>> futureList) {
        List<Playlist> completedPlaylists = new ArrayList<>();
        List<Map<String, Future<Playlist>>> retriedPlaylists = new ArrayList<>();
        for (Map<String, Future<Playlist>> stringFutureMap: futureList) {
            try {
                Playlist playlist = stringFutureMap.values().iterator().next().get();
                completedPlaylists.add(playlist);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            } catch (ExecutionException e) {
                log.error(e.getMessage(), e);

                if (e.getCause() instanceof TooManyRequestsException) {
                    String[] key = stringFutureMap.keySet().iterator().next().split("_");
                    retriedPlaylists.add(pullPlaylistAsync(api, key[0], key[1]));
                }
            }
        }
        if (!retriedPlaylists.isEmpty()) {
            completedPlaylists.addAll(getPlaylists(api, retriedPlaylists));
        }
        return completedPlaylists;
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


    private Map<String, Future<Playlist>> convertSimplePlaylistToPlaylist(SpotifyApi api, PlaylistSimplified simplePlaylist) {
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
        List<String> storedTracks = (List<String>) playlistDao.retrieveEntryById(newRefinedPlaylist.getId(), collectionName).get("trackIds");
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
        playlistDao.deleteEntryinDb(newRefinedPlaylist.getId(), playlistDao.getCollection(collectionName));
        playlistDao.saveEntryToDatabase(RefinedPlaylist.class, newRefinedPlaylist, playlistDao.getCollection(collectionName));
    }
}
