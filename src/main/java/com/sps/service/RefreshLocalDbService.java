package com.sps.service;

import com.neovisionaries.i18n.CountryCode;
import com.sps.model.RefinedPlaylist;
import com.sps.model.RefinedTrack;
import com.sps.model.RefinedUser;
import com.sps.repository.ArtistRepository;
import com.sps.repository.PlaylistRepository;
import com.sps.repository.TrackRepository;
import com.sps.repository.UserRepository;
import com.sps.utilities.DownstreamMapper;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.special.FeaturedPlaylists;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.data.artists.GetArtistRequest;
import com.wrapper.spotify.requests.data.artists.GetSeveralArtistsRequest;
import com.wrapper.spotify.requests.data.browse.GetListOfFeaturedPlaylistsRequest;
import com.wrapper.spotify.requests.data.playlists.GetListOfUsersPlaylistsRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import com.wrapper.spotify.requests.data.tracks.GetSeveralTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import com.wrapper.spotify.requests.data.users_profile.GetUsersProfileRequest;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class RefreshLocalDbService {

    //TODO 1. Catch when access token expires
    //TODO 3. Write UI
    //TODO 4. Write user comparision service/module
    //TODO 5. Write documentation
    //TODO 6. Find someplace to host

    PlaylistRepository playlistRepository;
    AuthenticationService authenticationService;
    UserRepository userRepository;
    TrackRepository trackRepository;
    ArtistRepository artistRepository;

    @Autowired
    public RefreshLocalDbService(AuthenticationService authenticationService, PlaylistRepository playlistRepository, UserRepository userRepository, TrackRepository trackRepository, ArtistRepository artistRepository) {
        this.authenticationService = authenticationService;
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
        this.trackRepository = trackRepository;
        this.artistRepository = artistRepository;
    }


    @Scheduled(fixedDelay = 43200)
    public void main() {
        SpotifyApi api = authenticationService.clientCredentialFlow();
        log.info("-----------------------Database refresh started-----------------------");
        List<RefinedPlaylist> playlistsToPull = playlistRepository.findByName(null);
        pullLocationPlaylists(api);
        pullFeaturedPlaylists(api);
        //TODO pull playlists from user.
        if (!playlistsToPull.isEmpty()) {
            for (RefinedPlaylist aRefinedPlaylistToPull : playlistsToPull) {
                pullPlaylist(api, aRefinedPlaylistToPull);
            }
        } else {
            log.info("No updates needed to local DB");
        }
    }

    private void pullLocationPlaylists(SpotifyApi api) {
        log.info("-----------------------Pulling Location Playlists-----------------------");
        List<PlaylistSimplified> playlists = pullPlaylistsForAUser(api, "thesoundsofspotify", 0);
        playlists.removeIf(SimplePlaylist -> !SimplePlaylist.getName().contains("The Sound of "));
        playlists.removeIf(SimplePlaylist -> SimplePlaylist.getName().matches("\\w*[A-Z][A-Z]\\b"));
        for (int i = 0; i < playlists.size(); i++) {
            if (playlists.get(i).getName().matches(".*[A-Z][A-Z]\\b")) {
                playlists.remove(i);
            }
        }
        saveSimplePlaylistsToDatastore(api, playlists);
    }

    private void pullFeaturedPlaylists(SpotifyApi api) {
        log.info("-----------------------Pulling featured Playlists-----------------------");
        Date timestamp = new Date();
        FeaturedPlaylists featuredPlaylists;

        final GetListOfFeaturedPlaylistsRequest request = api.getListOfFeaturedPlaylists()
                .limit(20)
                .offset(1)
                .country(CountryCode.GB)
                .offset(0)
                .timestamp(timestamp)
                .build();
        try {
            //Todo async
            featuredPlaylists = request.execute();
            List<PlaylistSimplified> featuredPlaylistList = Arrays.asList(featuredPlaylists.getPlaylists().getItems());
            saveSimplePlaylistsToDatastore(api, featuredPlaylistList);
        } catch (Exception e) {
            log.error("Failed to pull featured Playlists from Spotify Api, HTTP Status code: {}", e.getMessage());
        }
    }

    private void saveSpotifyPlaylist(SpotifyApi api, Playlist fullPlaylist) {
        RefinedPlaylist newRefinedPlaylist = DownstreamMapper.mapPlaylist(fullPlaylist);
        savePlaylist(api, newRefinedPlaylist);
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
                    trackRepository.saveAndFlush(DownstreamMapper.mapSong(track));
                    log.info("Saved Song: {} to the database", track.getName());
                }
            }
        }
        log.error(fullRefinedPlaylist.toString());
        playlistRepository.saveAndFlush(fullRefinedPlaylist);
        log.info("Saved a playlist called {}", fullRefinedPlaylist.getName());
    }

    private void saveSimplePlaylistsToDatastore(SpotifyApi api, List<PlaylistSimplified> playlistsToSave) {
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


    private User pullUser(SpotifyApi api, String id) {
        //Todo MakeAsync
        final GetUsersProfileRequest request = api.getUsersProfile(id).build();
        try {
            return request.execute();
        } catch (Exception e) {
            log.error("Cannot download user metadata with id {}, HTTP Status code: {}", id, e.getMessage());
            return null;
        }
    }

    public static Track pullSong(SpotifyApi api, String id) {
        final GetTrackRequest request = api.getTrack(id).build();
        try {
            return request.execute();
        } catch (Exception e) {
            log.error("Cannot download metadata for song with id {}, HTTP Status code: {}", id, e.getMessage());
            return null;
        }
    }

    public static List<Track> pullSongs(SpotifyApi api, ArrayList<String> ids) {
        List<Track> pulledTracks = new ArrayList<>();
        if (ids.size() > 50) {
            final GetSeveralTracksRequest request = api.getSeveralTracks(ids.subList(0, 49).toString()).build();
            ids = new ArrayList<>(ids.subList(49, ids.size()));
            try {
                pulledTracks.addAll(Arrays.asList(request.execute()));
            } catch (Exception e) {
                log.error("Cannot retrieve batch of songs from api, HTTP Status code: {}", e.getMessage());
                return null;
            }
            pulledTracks.addAll(pullSongs(api, ids));
            return pulledTracks;
        } else {
            final GetSeveralTracksRequest request = api.getSeveralTracks(ids.toString()).build();
            try {
                pulledTracks.addAll(Arrays.asList(request.execute()));
                return pulledTracks;
            } catch (Exception e) {
                log.error("Cannot retrieve batch of songs from api, HTTP error code: {}", e.getMessage());
               return null;
            }
        }
    }

    public static Artist pullArtist(SpotifyApi api, String id) {
        GetArtistRequest request = api.getArtist(id).build();
        try {
            return request.execute();
        } catch (Exception e) {
            log.error("Failed to download metadata for artist {}, HTTP error code: {} ", id, e.getMessage());
            return null;
        }

    }

    public static List<Artist> pullArtists(SpotifyApi api, List<String> ids) {
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
                return null;
            }
        }
    }

    private Playlist convertSimplePlaylistToPlaylist(SpotifyApi api, PlaylistSimplified simplePlaylist) {
        return pullPlaylist(api, simplePlaylist.getOwner().getId(), simplePlaylist.getId());
    }

    private List<PlaylistSimplified> pullPlaylistsForAUser(SpotifyApi api, String userId, int offset) {
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

}
