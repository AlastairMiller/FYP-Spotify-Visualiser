package com.sv.svi.service;

import com.mongodb.client.MongoCollection;
import com.neovisionaries.i18n.CountryCode;
import com.sv.svi.MongoHelper;
import com.sv.svi.SpotifyAuthenticationHelper;
import com.sv.svi.dao.SpotifyPlaylistDao;
import com.sv.svi.utils.DownstreamMapper;
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

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@Data
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RefreshDbService {

    private final SpotifyAuthenticationHelper authenticationHelper;
    private final MongoHelper mongoHelper;
    private final SpotifyPlaylistDao spotifyPlaylistDao;
    private SpotifyApi api;
    private ExecutorService executor = Executors.newFixedThreadPool(16);
    private int refreshRetries = 0;
    private Boolean refreshInProgress = false;

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
        MongoCollection<org.bson.Document> trackCollection = mongoHelper.getRefinedTrackDao().getMongoCollection();

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
            e.printStackTrace();
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
            spotifyPlaylistDao.getPlaylistsFromSimplePlaylists(api, featuredPlaylistList);
        } catch (Exception e) {
            log.error("Failed to pull featured Playlists from Spotify Api, HTTP Status code: {}", e.getMessage());
        }
    }


    private void savePlaylists(List<Playlist> playlists) throws MalformedURLException {
        for (Playlist playlist : playlists) {
            if (mongoHelper.getRefinedPlaylistDao().retrieveEntryById(playlist.getId()) == null) {
                try {
                    mongoHelper.getRefinedPlaylistDao().saveEntryToDatabase(DownstreamMapper.mapPlaylist(playlist));
                    log.info("Saved a playlist called {}", playlist.getName());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            } else {
               playlistCleanup(DownstreamMapper.mapPlaylist(playlist));
            }
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


}
