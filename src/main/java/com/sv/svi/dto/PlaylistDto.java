package com.sv.svi.dto;

import com.sv.svi.utils.DownstreamMapper;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.data.playlists.GetListOfUsersPlaylistsRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import lombok.extern.slf4j.Slf4j;
import refinedDataModels.RefinedPlaylist;
import refinedDataModels.RefinedTrack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class PlaylistDto {


    Playlist pullPlaylist(SpotifyApi api, RefinedPlaylist refinedPlaylistToPull) {
        return pullPlaylist(api, refinedPlaylistToPull.getRefinedUser().getId(), refinedPlaylistToPull.getId());
    }

    private Playlist pullPlaylist(SpotifyApi api, String ownerId, String playlistId) {
        //Todo make Async
        final GetPlaylistRequest request = api.getPlaylist(ownerId, playlistId).build();
        try {
            return request.execute();
        } catch (Exception e) {
            log.error("Failed to download metadata for playlist {}, HTTP error code: {}", playlistId, e.getMessage());
            return null;
        }

    }

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

    private Playlist convertSimplePlaylistToPlaylist(SpotifyApi api, PlaylistSimplified simplePlaylist) {
        return pullPlaylist(api, simplePlaylist.getOwner().getId(), simplePlaylist.getId());
    }

    private void playlistCleanup(SpotifyApi api, RefinedPlaylist newRefinedPlaylist) {
        List<RefinedTrack> storedTracks = playlistRepository.findOne(newRefinedPlaylist.getId()).getRefinedTracks();
        for (int i = 0; i < storedTracks.size(); i++) {
            Boolean remove = true;
            for (int p = 0; p < newRefinedPlaylist.getRefinedTracks().size(); p++) {
                if (storedTracks.get(i).getId().equals(newRefinedPlaylist.getRefinedTracks().get(p).getId())) {
                    remove = false;
                    p = newRefinedPlaylist.getRefinedTracks().size() - 1;
                }
            }
            if (remove) {
                log.info("Removing song: {} from playlist: {}", storedTracks.get(i).getName(), newRefinedPlaylist.getName());
                storedTracks.remove(i);
            }
        }
        playlistRepository.delete(newRefinedPlaylist.getId());
        savePlaylist(api, newRefinedPlaylist);
    }

    public void saveSimplePlaylistsToDatastore(SpotifyApi api, List<PlaylistSimplified> playlistsToSave) {
        for (PlaylistSimplified simplePlaylist : playlistsToSave) {
            Playlist fullPlaylist = convertSimplePlaylistToPlaylist(api, simplePlaylist);
            if (fullPlaylist != null) {
                if (playlistRepository.findById(fullPlaylist.getId()) == null) {
                    saveSpotifyPlaylist(api, fullPlaylist);
                } else {
                    playlistCleanup(api, DownstreamMapper.mapPlaylist(fullPlaylist));
                }
            }
        }
    }


    private void savePlaylist(SpotifyApi api, RefinedPlaylist fullRefinedPlaylist) {

        if (playlistRepository.findById(fullRefinedPlaylist.getId()) != null) {
            playlistCleanup(api, fullRefinedPlaylist);
        }

        if (userRepository.findById(fullRefinedPlaylist.getRefinedUser().getId()) == null) {

            RefinedUser newRefinedUser = DownstreamMapper.mapUser(pullUser(api, fullRefinedPlaylist.getRefinedUser().getId()));
            userRepository.saveAndFlush(newRefinedUser);

        }
        ArrayList<String> songIdArrayList = new ArrayList<>();
        for (int i = 0; i < fullRefinedPlaylist.getRefinedTracks().size(); i++) {
            if (trackRepository.findById(fullRefinedPlaylist.getRefinedTracks().get(i).getId()) == null) {
                songIdArrayList.add(fullRefinedPlaylist.getRefinedTracks().get(i).getId());
            }
        }
        if (songIdArrayList.size() > 0) {
            List<Track> trackArrayList = pullSongs(api, songIdArrayList);
            if (trackArrayList != null) {
                ArrayList<String> artistIdArrayList = new ArrayList<String>();
                for (Track track : trackArrayList) {
                    if (track != null) {
                        for (int p = 0; p < track.getArtists().length; p++) {
                            if (artistRepository.findById(track.getArtists()[p].getId()) == null) {
                                artistIdArrayList.add(track.getArtists()[p].getId());
                            }
                        }
                    }
                }

                List<Artist> artistList = pullArtists(api, artistIdArrayList);
                if (artistList != null) {
                    for (Artist artist : artistList) {
                        artistRepository.saveAndFlush(DownstreamMapper.mapArtist(artist));
                        log.info("Saved Artist: {} to the database", artist.getName());
                    }
                }

                for (Track track : trackArrayList) {
                    trackRepository.saveAndFlush(DownstreamMapper.mapTrack(track));
                    log.info("Saved Song: {} to the database", track.getName());
                }
            }
        }
        log.error(fullRefinedPlaylist.toString());
        playlistRepository.saveAndFlush(fullRefinedPlaylist);
        log.info("Saved a playlist called {}", fullRefinedPlaylist.getName());
    }

    private void saveSpotifyPlaylist(SpotifyApi api, Playlist fullPlaylist) {
        RefinedPlaylist newRefinedPlaylist = DownstreamMapper.mapPlaylist(fullPlaylist);
        savePlaylist(api, newRefinedPlaylist);
    }


}
