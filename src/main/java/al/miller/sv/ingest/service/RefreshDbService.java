package al.miller.sv.ingest.service;

import al.miller.sv.ds.dao.AbstractDao;
import al.miller.sv.ds.dao.RefinedPlaylistDao;
import al.miller.sv.ingest.MongoHelper;
import al.miller.sv.ingest.SpotifyAuthenticationHelper;
import al.miller.sv.ingest.dao.*;
import al.miller.sv.ingest.utils.DownstreamMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;
import com.neovisionaries.i18n.CountryCode;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.special.FeaturedPlaylists;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.data.browse.GetListOfFeaturedPlaylistsRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
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
    private MongoCollection<PlaylistSimplified> batchCollection;

    private List<String> trackIdsToGet = new ArrayList<>();
    private List<String> albumIdsToPull = new ArrayList<>();
    private List<String> artistIdsToPull = new ArrayList<>();
    private List<Future<RefinedAlbum>> albumFutures = new ArrayList<>();
    private List<Future<?>> futureListOfNestedElements = new ArrayList<>();

    @Scheduled(fixedDelay = 360000)
    @PostConstruct
    public void refreshToken() {
        log.info("-----------------------Refreshing Api Token-----------------------");
        api = authenticationHelper.clientCredentialFlow();
        if (api == null) {
            throw new RuntimeException("Cannot refresh Auth Token. API down?");
        }
    }

    @Scheduled(fixedDelay = 30000000)
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
        List<String> indexList = checkIfIndexesExists(mongoHelper.getRefinedAlbumDao());
        for (String indexName : indexList) {
            mongoHelper.getRefinedAlbumDao().getMongoCollection().createIndex(Indexes.descending(indexName));
            mongoHelper.getRefinedAlbumDao().getMongoCollection().createIndex(Indexes.descending("_id", indexName));
            log.info("Created {} index for Album Id", indexName);
        }
        indexList = checkIfIndexesExists(mongoHelper.getRefinedArtistDao());
        for (String indexName : indexList) {
            mongoHelper.getRefinedArtistDao().getMongoCollection().createIndex(Indexes.descending(indexName));
            mongoHelper.getRefinedArtistDao().getMongoCollection().createIndex(Indexes.descending("_id", indexName));
            log.info("Created {} index for Artist Id", indexName);
        }

        indexList = checkIfIndexesExists(mongoHelper.getRefinedPlaylistDao());
        for (String indexName : indexList) {
            mongoHelper.getRefinedPlaylistDao().getMongoCollection().createIndex(Indexes.descending(indexName));
            mongoHelper.getRefinedPlaylistDao().getMongoCollection().createIndex(Indexes.descending("_id", indexName));
            log.info("Created {} index for Playlist Id", indexName);
        }

        indexList = checkIfIndexesExists(mongoHelper.getRefinedTrackDao());
        for (String indexName : indexList) {
            mongoHelper.getRefinedTrackDao().getMongoCollection().createIndex(Indexes.descending(indexName));
            mongoHelper.getRefinedTrackDao().getMongoCollection().createIndex(Indexes.descending("_id", indexName));
            log.info("Created {} index for Track Id", indexName);
        }

    }

    private void pullLocationPlaylists(SpotifyApi api) {
        log.info("-----------------------Pulling location playlist's-----------------------");
        int playlistRetrieval = 0;
        //Write batches to collection
        List<PlaylistSimplified> playlistSimplifieds = spotifyPlaylistDao.pullPlaylistsForAUser(api, "thesoundsofspotify", playlistRetrieval).stream()
                .filter(playlist -> playlist.getName().matches(".* [A-Z][A-Z]\\b"))
                .collect(Collectors.toList());

        mongoHelper.getPlaylistSimplifiedDao().getMongoCollection().drop();
        List<BsonDocument> playlistIds = playlistSimplifieds.stream()
                .map(temp -> {
                    BsonDocument document = new BsonDocument();
                    document.append("id", new BsonString(temp.getId()));
                    document.append("ownerId", new BsonString(temp.getOwner().getId()));
                    return document;
                })
                .collect(Collectors.toList());

        mongoHelper.getPlaylistSimplifiedDao().saveList(playlistIds);
        playlistSimplifieds.clear();

        ArrayList<BsonDocument> playlistSimplifiedIdList = mongoHelper.getPlaylistSimplifiedDao().getMongoCollection().find().into(new ArrayList<>());
        for (BsonDocument playlistSimplifiedDocument : playlistSimplifiedIdList) {
            try {
                Playlist playlist = SpotifyPlaylistDao.pullPlaylistSync(api, playlistSimplifiedDocument.get("ownerId").asString().getValue(), playlistSimplifiedDocument.get("id").asString().getValue());
                savePlaylist(playlist);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            log.info("{} playlists in the database", mongoHelper.getRefinedPlaylistDao().getMongoCollection().count());
            log.info("{} albums in the database", mongoHelper.getRefinedAlbumDao().getMongoCollection().count());
            log.info("{} tracks in the database", mongoHelper.getRefinedTrackDao().getMongoCollection().count());
            log.info("{} artists in the database", mongoHelper.getRefinedArtistDao().getMongoCollection().count());
        }
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

    private void savePlaylist(Playlist playlist) throws IllegalAccessException, InstantiationException, MalformedURLException {
        savePlaylists(Collections.singletonList(playlist));
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
                trackIdsToGet.add(refinedTrackId);
            }
        }
        if (trackIdsToGet.size() > 0) {
            try {
                List<Track> tracks = spotifyTrackDao.pullTracksAsync(api, trackIdsToGet);
                saveTracks(tracks);
                for (Track track : tracks) {
                    verifyAlbumIsPersisted(track.getAlbum().getId());
                    List<String> artistIds = Arrays.stream(track.getArtists())
                            .map(ArtistSimplified::getId)
                            .collect(Collectors.toList());
                    verifyArtistsArePersisted(artistIds);
                }
                trackIdsToGet.clear();
            } catch (Exception e) {
                log.error(e.getMessage());
            }

        }
    }

    private void verifyArtistsArePersisted(List<String> artistsIds) {
        for (String artistId : artistsIds) {
            if (mongoHelper.getRefinedArtistDao().getById(artistId) == null && !artistIdsToPull.contains(artistId)) {
                artistIdsToPull.add(artistId);
            }
        }
        if (artistIdsToPull.size() >= 50) {
            try {
                log.info("verifyArtistsArePersisted before subList: "+ artistIdsToPull.toString());
                List<Artist> artists = spotifyArtistDao.pullArtistsAsync(api, artistIdsToPull);
                saveArtists(artists);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            artistIdsToPull.subList(0, 49).clear();
            log.info("verifyArtistsArePersisted after subList: "+artistsIds.toString());
        }
    }


    private void verifyAlbumIsPersisted(String albumId) {
        if (mongoHelper.getRefinedAlbumDao().getById(albumId) == null && !albumIdsToPull.contains(albumId)) {
            albumIdsToPull.add(albumId);
        }
        if (albumIdsToPull.size() == 20) {
            try {
                List<Album> albums = spotifyAlbumDao.pullAlbumsAsync(api, albumIdsToPull);
                saveAlbums(albums);
                albumIdsToPull.clear();
                List<String> trackIds = albums.stream()
                        .flatMap(album -> Arrays.stream(album.getTracks().getItems()))
                        .map(TrackSimplified::getId)
                        .collect(Collectors.toList());
                verifyTracksArePersisted(trackIds);
            } catch (Exception e) {
                log.error(e.getMessage());
            }

        }
    }

    private <T extends AbstractDao> List<String> checkIfIndexesExists(T dao) {
        List<Document> daosIndexes = (List<Document>) dao.getMongoCollection().listIndexes().into(new ArrayList());
        List<String> indexList = new ArrayList<>();
        indexList.add("id");
        if (dao.getClazz().equals(RefinedPlaylist.class)) {
            indexList.add("numOfFollowers");
        } else {
            indexList.add("popularity");
        }
        daosIndexes.forEach(document -> indexList.remove(document.getString("name")));
        return indexList;
    }
}
