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
import org.bson.Document;
import refinedDataModels.RefinedPlaylist;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@NoArgsConstructor
public class PlaylistDto {
    private static final String collectionName = "RefinedPlaylists";

    public List<PlaylistSimplified> pullPlaylistsForAUser(SpotifyApi api, String userId, int offset) {
        final GetListOfUsersPlaylistsRequest request = api.getListOfUsersPlaylists(userId).limit(50).offset(offset).build();
        try {
            //Todo MakeAsync
            List<PlaylistSimplified> usersPlaylist = new ArrayList<>(Arrays.asList(request.execute().getItems()));
            log.info("Pulled {} playlists for user {}", offset, userId);
            if (usersPlaylist.size() == 50) {
                usersPlaylist.addAll(pullPlaylistsForAUser(api, userId, offset + 50));
            }
            return usersPlaylist;
        } catch (Exception e) {
            log.error("Failed to grab user's: {} playlist", userId);
            return null;
        }
    }


    public void saveSimplePlaylistsToMongo(SpotifyApi api, List<PlaylistSimplified> playlistsToSave, MongoDao playlistDao) {
        List<Playlist> completedPlaylists = new ArrayList<>();
        for (PlaylistSimplified simplePlaylist : playlistsToSave) {
            completedPlaylists.add(convertSimplePlaylistToPlaylist(api, simplePlaylist));
            log.info("Pulled Playlist: {}", simplePlaylist.getName());
        }
        savePlaylists(api, completedPlaylists, playlistDao);
    }

    public static Playlist pullPlaylist(SpotifyApi api, String ownerId, String playlistId) {
        //Todo make Async
        final GetPlaylistRequest request = api.getPlaylist(ownerId, playlistId).build();
        try {
            return request.execute();
        } catch (Exception e) {
            log.error("Failed to download metadata for playlist {}, HTTP error code: {}", playlistId, e.getMessage());
            return null;
        }

    }

    private Playlist convertSimplePlaylistToPlaylist(SpotifyApi api, PlaylistSimplified simplePlaylist) {
        return pullPlaylist(api, simplePlaylist.getOwner().getId(), simplePlaylist.getId());
    }


    public void savePlaylists(SpotifyApi api, List<Playlist> playlists, MongoDao playlistDao) {
        MongoCollection mongoCollection = playlistDao.getCollection(collectionName);
        for (Playlist playlist : playlists) {
            if (playlistDao.retrieveEntryById(playlist.getId(), collectionName) == null) {
                try {
                    playlistDao.saveEntryToDatabase(RefinedPlaylist.class, DownstreamMapper.mapPlaylist(playlist), mongoCollection);
                    log.info("Saved a playlist called {}", playlist.getName());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }else {
                //Playlist Cleanup
            }
        }
    }


    private void playlistCleanup(SpotifyApi api, RefinedPlaylist newRefinedPlaylist) {
//        List<RefinedTrack> storedTracks = playlistRepository.findOne(newRefinedPlaylist.getId()).getRefinedTracks();
//        for (int i = 0; i < storedTracks.size(); i++) {
//            Boolean remove = true;
//            for (int p = 0; p < newRefinedPlaylist.getRefinedTracks().size(); p++) {
//                if (storedTracks.get(i).getId().equals(newRefinedPlaylist.getRefinedTracks().get(p).getId())) {
//                    remove = false;
//                    p = newRefinedPlaylist.getRefinedTracks().size() - 1;
//                }
//            }
//            if (remove) {
//                log.info("Removing song: {} from playlist: {}", storedTracks.get(i).getName(), newRefinedPlaylist.getName());
//                storedTracks.remove(i);
//            }
//        }
//        playlistRepository.delete(newRefinedPlaylist.getId());
//        savePlaylist(api, newRefinedPlaylist);
    }

}
