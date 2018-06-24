package al.miller.sv.ingest.service;

import al.miller.sv.ingest.dao.*;
import com.mongodb.client.model.Indexes;
import com.neovisionaries.i18n.CountryCode;
import al.miller.sv.ingest.MongoHelper;
import al.miller.sv.ingest.SpotifyAuthenticationHelper;
import al.miller.sv.ingest.utils.DownstreamMapper;
import com.svd.dao.AbstractDao;
import com.svd.dao.RefinedUserDao;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.special.FeaturedPlaylists;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.data.browse.GetListOfFeaturedPlaylistsRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import refinedDataModels.RefinedAlbum;
import refinedDataModels.RefinedArtist;
import refinedDataModels.RefinedPlaylist;
import refinedDataModels.RefinedTrack;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Slf4j
@Service
@Data
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RefreshDbService {

    private final SpotifyAuthenticationHelper authenticationHelper;
    private final MongoHelper mongoHelper;
    private final SpotifyAlbumDao spotifyAlbumDao;
    private final SpotifyArtistDao spotifyArtistDao;
    private final SpotifyPlaylistDao spotifyPlaylistDao;
    private final SpotifyTrackDao spotifyTrackDao;
    private final SpotifyUserDao spotifyUserDao;
    private SpotifyApi api;
    private ExecutorService executor = Executors.newFixedThreadPool(16);
    private int refreshRetries = 0;
    private Boolean refreshInProgress = false;

    private List<String> trackIdsToGet = new ArrayList<>();
    private List<Future<RefinedAlbum>> albumFutures = new ArrayList<>();
    private List<Future<?>> futureListOfNestedElements = new ArrayList<>();

    @Scheduled(fixedDelay = 3600000)
    @PostConstruct
    public void refreshToken() {
        log.info("-----------------------Refreshing Api Token-----------------------");
        api = authenticationHelper.clientCredentialFlow();
        if (api == null) {
            throw new RuntimeException("Cannot refresh Auth Token. API down?");
        }
    }

    @Scheduled(fixedDelay = 300000)
    private void refreshDB() {
        refreshInProgress = true;
        if (refreshRetries <= 100) {
            refreshRetries = 0;
            log.info("-----------------------Database refresh started-----------------------");
            createIndexes();
            pullLocationPlaylists(api);
            pullFeaturedPlaylists(api);
            refreshInProgress = false;
            log.info("Finished DB refresh");
        } else {
            log.error("Request timed out after {} retries", refreshRetries);
        }
    }

    private void createIndexes() {
        log.info("----------------------------Created Indexes----------------------------");
        if (checkIfIndexExists(mongoHelper.getRefinedAlbumDao())) {
            mongoHelper.getRefinedAlbumDao().getMongoCollection().createIndex(Indexes.ascending("id"));
            log.info("Created index for Album Id");
        }
        if (checkIfIndexExists(mongoHelper.getRefinedArtistDao())) {
            mongoHelper.getRefinedArtistDao().getMongoCollection().createIndex(Indexes.ascending(("id")));
            log.info("Created index for Artist Id");
        }
        if (checkIfIndexExists(mongoHelper.getRefinedPlaylistDao())) {
            mongoHelper.getRefinedPlaylistDao().getMongoCollection().createIndex(Indexes.ascending("id"));
            log.info("Created index for Playlist Id");
        }
        if (checkIfIndexExists(mongoHelper.getRefinedTrackDao())) {
            mongoHelper.getRefinedTrackDao().getMongoCollection().createIndex(Indexes.ascending("id"));
            log.info("Created index for Track Id");
        }
        if (checkIfIndexExists(mongoHelper.getRefinedUserDao())) {
            mongoHelper.getRefinedUserDao().getMongoCollection().createIndex(Indexes.ascending("id"));
            log.info("Created index for User Id");
        }

    }

    private void pullLocationPlaylists(SpotifyApi api) {
        log.info("-----------------------Pulling location playlist's-----------------------");
        int playlistRetrieval = 0;
        List<PlaylistSimplified> playlistSimplifieds = spotifyPlaylistDao.pullPlaylistsForAUser(api, "thesoundsofspotify", playlistRetrieval).stream()
                .filter(playlist -> playlist.getName().matches(".* [A-Z][A-Z]\\b"))
                .collect(Collectors.toList());
//       playlistSimplifieds.subList(1000, playlistSimplifieds.size()).clear();
        List<Playlist> playlists = spotifyPlaylistDao.getPlaylistsFromSimplePlaylists(api, playlistSimplifieds);
        try {
            savePlaylists(playlists);
        } catch (Exception e) {
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
            List<PlaylistSimplified> featuredPlaylistList = asList(featuredPlaylists.getPlaylists().getItems());
            List<Playlist> playlists = spotifyPlaylistDao.getPlaylistsFromSimplePlaylists(api, featuredPlaylistList);
            savePlaylists(playlists);
        } catch (Exception e) {
            log.error("Failed to pull featured Playlists from Spotify Api, Status code: {}", e.getMessage());
        }
    }

    private void savePlaylists(List<Playlist> playlists) throws MalformedURLException, IllegalAccessException, InstantiationException {
        for (Playlist playlist : playlists) {
            RefinedPlaylist refinedPlaylist = DownstreamMapper.mapPlaylist(playlist);
            if (mongoHelper.getRefinedPlaylistDao().getById(playlist.getId()) == null) {
                mongoHelper.getRefinedPlaylistDao().save(refinedPlaylist);
                log.info("Saved a playlist called {}", playlist.getName());
            } else {
                playlistCleanup(DownstreamMapper.mapPlaylist(playlist));
            }
            verifyTracksArePersisted(refinedPlaylist.getTrackIds());
        }
    }

    private void saveTracks(List<Track> tracks) throws MalformedURLException, IllegalAccessException, InstantiationException {
        for (Track track : tracks) {
            RefinedTrack refinedTrack = DownstreamMapper.mapTrack(track);
            mongoHelper.getRefinedTrackDao().save(refinedTrack);
            log.info("Saved a track called {}", track.getName());
        }
    }

    private void saveAlbums(List<Album> albums) throws MalformedURLException, IllegalAccessException, InstantiationException {
        for (Album album : albums) {
            RefinedAlbum refinedAlbum = DownstreamMapper.mapAlbum(album);
            mongoHelper.getRefinedAlbumDao().save(refinedAlbum);
            log.info("Saved a album called {}", album.getName());
        }
    }

    private void saveArtists(List<Artist> artists) throws MalformedURLException, IllegalAccessException, InstantiationException {
        for (Artist artist : artists) {
            RefinedArtist refinedArtist = DownstreamMapper.mapArtist(artist);
            mongoHelper.getRefinedArtistDao().save(refinedArtist);
            log.info("Saved a artist called {}", artist.getName());
        }
    }


    private void playlistCleanup(RefinedPlaylist newRefinedPlaylist) {
        List<String> storedTracks = mongoHelper.getRefinedPlaylistDao().getById(newRefinedPlaylist.getId()).getTrackIds();
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
        mongoHelper.getRefinedPlaylistDao().delete(newRefinedPlaylist.getId());
        mongoHelper.getRefinedPlaylistDao().save(newRefinedPlaylist);
    }

    private void verifyTracksArePersisted(List<String> trackIds) {
        for (String refinedTrackId : trackIds) {
            if (mongoHelper.getRefinedTrackDao().getById(refinedTrackId) == null && !trackIdsToGet.contains(refinedTrackId)) {
                log.info("Track {} missing, will get.", refinedTrackId);
                trackIdsToGet.add(refinedTrackId);
            } else {
                log.info("Track {} already exists in Mongo.", refinedTrackId);
            }
        }
        if (trackIdsToGet.size() > 0) {
            List<Track> tracks = spotifyTrackDao.pullTracksAsync(api, trackIdsToGet);
            try {
                saveTracks(tracks);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            for (Track track : tracks) {
                verifyAlbumIsPersisted(track.getAlbum().getId());
                List<String> artistIds = Arrays.stream(track.getArtists())
                        .map(ArtistSimplified::getId)
                        .collect(Collectors.toList());
                verifyArtistsArePersisted(artistIds);
            }
        }
    }

    private void verifyArtistsArePersisted(List<String> artistsIds) {
        List<String> artistIdsToPull = new ArrayList<>();
        for (String artistId : artistsIds) {
            if (mongoHelper.getRefinedArtistDao().getById(artistId) == null && !artistsIds.contains(artistId)) {
                log.info("Artist {} missing, will get.", artistId);
                artistIdsToPull.add(artistId);
            } else {
                log.info("Artist {} already exists", artistId);
            }
        }
        if (artistIdsToPull.size() >= 50) {
            List<Artist> artists = spotifyArtistDao.pullArtistsAsync(api, artistIdsToPull);
            try {
                saveArtists(artists);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            artistIdsToPull.subList(0, 49).clear();
        }
    }


    private void verifyAlbumIsPersisted(String albumId) {
        List<String> albumIdsToPull = new ArrayList<>();
        if (mongoHelper.getRefinedAlbumDao().getById(albumId) == null && !albumIdsToPull.contains(albumId)) {
            log.info("Album {} missing, will get.", albumId);
            albumIdsToPull.add(albumId);
        }
        if (albumIdsToPull.size() == 20) {
            List<Album> albums = spotifyAlbumDao.pullAlbumsAsync(api, albumIdsToPull);
            try {
                saveAlbums(albums);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            albumIdsToPull.clear();
            List<String> trackIds = albums.stream()
                    .flatMap(album -> Arrays.stream(album.getTracks().getItems()))
                    .map(TrackSimplified::getId)
                    .collect(Collectors.toList());
            verifyTracksArePersisted(trackIds);
        }
    }

    private <T extends AbstractDao> boolean checkIfIndexExists(T dao) {
        Collection daosIndexes = dao.getMongoCollection().listIndexes().into(new ArrayList());
        return daosIndexes.size() == 0;
    }

    private boolean checkIfIndexExists(RefinedUserDao dao) {
        Collection daosIndexes = dao.getMongoCollection().listIndexes().into(new ArrayList<>());
        return daosIndexes.size() == 0;
    }

}
