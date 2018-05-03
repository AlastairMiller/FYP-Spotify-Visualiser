package com.sv.svi.service;

import com.mongodb.client.MongoCollection;
import com.neovisionaries.i18n.CountryCode;
import com.sv.svi.MongoHelper;
import com.sv.svi.SpotifyAuthenticationHelper;
import com.sv.svi.dao.SpotifyArtistDao;
import com.sv.svi.dao.SpotifyPlaylistDao;
import com.sv.svi.dao.SpotifyTrackDao;
import com.sv.svi.utils.DownstreamMapper;
import com.svd.mapper.JsontoRefinedMapper;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.special.FeaturedPlaylists;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.requests.data.browse.GetListOfFeaturedPlaylistsRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import refinedDataModels.RefinedPlaylist;
import refinedDataModels.RefinedTrack;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@Service
@Data
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RefreshDbService {

    private final SpotifyAuthenticationHelper authenticationHelper;
    private final MongoHelper mongoHelper;
    private final SpotifyPlaylistDao spotifyPlaylistDao;
    private final SpotifyTrackDao spotifyTrackDao;
    private SpotifyApi api;
    private ExecutorService executor = Executors.newFixedThreadPool(16);
    private int refreshRetries = 0;
    private Boolean refreshInProgress = false;
    private List<?> spam = Collections.synchronizedList(new ArrayList<>());
    private List<Future<?>> futureListOfNestedElements = new ArrayList<>();

    @Scheduled(fixedDelay = 360000)
    @PostConstruct
    public void refreshToken() {
        log.info("-----------------------Refreshing Api Token-----------------------");
        api = authenticationHelper.clientCredentialFlow();
        if (api == null) {
            throw new RuntimeException("API is borked");
        }
    }

    @Scheduled(fixedDelay = 300000)
    private void refreshDB() {
        refreshInProgress = true;
        if (refreshRetries <= 100) {
            refreshRetries = 0;
            log.info("-----------------------Database refresh started-----------------------");
            pullLocationPlaylists(api);
            pullFeaturedPlaylists(api);
            refreshInProgress = false;
        } else {
            log.error("Request timed out after {} retries", refreshRetries);
        }
    }

    private void createIndexes() {
        log.info("----------------------------Creating Indexes----------------------------");
        MongoCollection trackCollection = mongoHelper.getRefinedTrackDao().getMongoCollection();

    }

    private void pullLocationPlaylists(SpotifyApi api) {
        log.info("-----------------------Pulling location playlist's-----------------------");
        int playlistRetrieval = 0;
        List<PlaylistSimplified> playlistSimplifieds = spotifyPlaylistDao.pullPlaylistsForAUser(api, "thesoundsofspotify", playlistRetrieval).stream()
                .filter(playlist -> playlist.getName().matches(".* [A-Z][A-Z]\\b"))
                .collect(Collectors.toList());
        List<Playlist> playlists = spotifyPlaylistDao.getPlaylistsFromSimplePlaylists(api, playlistSimplifieds);
        try {
            savePlaylists(playlists);
        } catch (MalformedURLException e) {
            log.error(e.getMessage());
        }
        log.info("{} playlists in the database", mongoHelper.getRefinedPlaylistDao().getMongoCollection().count());
    }

    private void pullFeaturedPlaylists(SpotifyApi api) {
        log.info("-----------------------Pulling featured playlist's-----------------------");
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
            featuredPlaylists = request.execute();
            List<PlaylistSimplified> featuredPlaylistList = Arrays.asList(featuredPlaylists.getPlaylists().getItems());
            List<Playlist> playlists = spotifyPlaylistDao.getPlaylistsFromSimplePlaylists(api, featuredPlaylistList);
            savePlaylists(playlists);
        } catch (Exception e) {
            log.error("Failed to pull featured Playlists from Spotify Api, Status code: {}", e.getMessage());
        }
    }

    private void savePlaylists(List<Playlist> playlists) throws MalformedURLException {
        for (Playlist playlist : playlists) {
            RefinedPlaylist refinedPlaylist = DownstreamMapper.mapPlaylist(playlist);
            if (mongoHelper.getRefinedPlaylistDao().retrieveEntryById(playlist.getId()) == null) {
                mongoHelper.getRefinedPlaylistDao().saveEntryToDatabase(refinedPlaylist);
                log.info("Saved a playlist called {}", playlist.getName());
            } else {
                playlistCleanup(DownstreamMapper.mapPlaylist(playlist));
            }
            verifyTracksArePersisted(refinedPlaylist);
        }
    }


    private void playlistCleanup(RefinedPlaylist newRefinedPlaylist) {
        List<String> storedTracks = (List<String>) mongoHelper.getRefinedPlaylistDao().retrieveEntryById(newRefinedPlaylist.getId()).get("trackIds");
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
        mongoHelper.getRefinedPlaylistDao().deleteEntryinDb(newRefinedPlaylist.getId(), mongoHelper.getRefinedPlaylistDao().getMongoCollection());
        mongoHelper.getRefinedPlaylistDao().saveEntryToDatabase(newRefinedPlaylist);
    }

    private void verifyTracksArePersisted(List<String> trackIds) {
        ArrayList<String> trackIdsToGet = new ArrayList<>();
        for (String refinedTrackId : trackIds) {
            if (mongoHelper.getRefinedTrackDao().retrieveEntryById(refinedTrackId) == null) {
                log.info("Track {} missing, will get.", refinedTrackId);
                trackIdsToGet.add(refinedTrackId);
            } else {
                log.info("Track {} already exists in Mongo.", refinedTrackId);
            }
            //split into 50 and make request async
            verifyAlbumsArePersisted();
            verifyArtistsArePersisted(JsontoRefinedMapper.toTrack(mongoHelper.getRefinedTrackDao().retrieveEntryById(refinedTrackId)).getArtistIds());
        }
    }

    private void verifyArtistsArePersisted(List<String> artistsIds) {
        ArrayList<String> artistIdsToGet = new ArrayList<>();
        for (String artistId : artistsIds) {
            if (mongoHelper.getRefinedArtistDao().retrieveEntryById(artistId) == null) {
                log.info("Artist {} missing, will get.", artistId);
                artistIdsToGet.add(artistId);
            } else {
                log.info("Artist {} already exists", artistId);
            }
            //split into 50 and make request async
        }

    }

    private void verifyAlbumsArePersisted(RefinedTrack track) {
        if (mongoHelper.getRefinedAlbumDao().retrieveEntryById(track.getAlbumId()) == null) {
            log.info("Album {} missing, will get");

        }

    }


}
