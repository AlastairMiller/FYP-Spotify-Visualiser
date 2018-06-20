package al.miller.sv.ingest;

import al.miller.sv.ingest.dao.*;
import al.miller.sv.ingest.utils.DownstreamMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.svd.ClientHandler;
import com.svd.dao.MongoDao;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.*;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import refinedDataModels.*;

import java.io.IOException;

import static com.svd.mapper.JsontoRefinedMapper.*;

@Slf4j
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@PropertySource("classpath:application.properties")
public class e2e {

    @Autowired
    private SpotifyAuthenticationHelper authenticationHelper;
    @Autowired
    private MongoHelper mongoHelper;
    private SpotifyAlbumDao spotifyAlbumDao;
    private SpotifyArtistDao spotifyArtistDao;
    private SpotifyPlaylistDao spotifyPlaylistDao;
    private SpotifyTrackDao spotifyTrackDao;
    private SpotifyUserDao spotifyUserDao;
    private SpotifyApi api;

    private MongodExecutable mongodExe;
    private MongodProcess mongod;
    private MongoDao<RefinedAlbum> albumDao;
    private MongoDao<RefinedArtist> artistDao;
    private MongoDao<RefinedPlaylist> playlistDao;
    private MongoDao<RefinedTrack> trackDao;
    private MongoDao<RefinedUser> userDao;
    private String hostname = "127.0.0.1";
    private int port = 12345;

    private static final MongodStarter starter = MongodStarter.getDefaultInstance();

    @Value("${clientId}")
    String clientId;
    @Value("${clientSecret}")
    String clientSecret;


    @Before
    public void beforeEach() throws IOException {
        this.authenticationHelper = new SpotifyAuthenticationHelper(clientId, clientSecret);
        this.api = authenticationHelper.clientCredentialFlow();
        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(hostname, port, Network.localhostIsIPv6()))
                .build();

        mongodExe = starter.prepare(mongodConfig);
        mongod = mongodExe.start();
        MongoClient mongoClient = new MongoClient(hostname, port);
        albumDao = new MongoDao<>(new ClientHandler(hostname, port, "embedded"), "RefinedAlbums", RefinedAlbum.class);
        artistDao = new MongoDao<>(new ClientHandler(hostname, port, "embedded"), "RefinedArtists", RefinedArtist.class);
        playlistDao = new MongoDao<>(new ClientHandler(hostname, port, "embedded"), "RefinedPlaylists", RefinedPlaylist.class);
        trackDao = new MongoDao<>(new ClientHandler(hostname, port, "embedded"), "RefinedTracks", RefinedTrack.class);
        userDao = new MongoDao<>(new ClientHandler(hostname, port, "embedded"), "RefinedUsers", RefinedUser.class);
    }

    @After
    public void afterEach() {
        if (this.mongod != null) {
            this.mongod.stop();
            this.mongodExe.stop();
        }
    }


    @Test
    public void shouldPullAndSaveAlbum() throws Exception {
        Album album = SpotifyAlbumDao.pullAlbum(api, "0gtU58GVllHFpZzIgLd5lV");
        assert album != null;
        RefinedAlbum expectedAlbum = DownstreamMapper.mapAlbum(album);
        MongoCollection<Document> mongoCollection = albumDao.getMongoCollection();
        albumDao.saveEntryToDatabase(expectedAlbum);
        Object albumObject = albumDao.retrieveEntryById("0gtU58GVllHFpZzIgLd5lV");
        RefinedAlbum actualAlbum = toAlbum((Document) albumObject);
        Assert.assertEquals(expectedAlbum, actualAlbum);
    }

    @Test
    public void shouldPullAndSaveArtist() throws Exception {
        Artist artist = SpotifyArtistDao.pullArtist(api, "5WId4o5jdGVhptNU0uqKxu");
        assert artist != null;
        RefinedArtist expectedArtist = DownstreamMapper.mapArtist(artist);
        artistDao.saveEntryToDatabase(expectedArtist);
        Object artistObject = artistDao.retrieveEntryById("5WId4o5jdGVhptNU0uqKxu");
        RefinedArtist actualArtist = toArtist((Document) artistObject);
        Assert.assertEquals(expectedArtist, actualArtist);
    }

    @Test
    public void shouldPullAndSavePlaylist() throws Exception {
        Playlist playlist = SpotifyPlaylistDao.pullPlaylist(api, "millersinc", "2CTdEa3JWbncC1h8WjnuxZ");
        assert playlist != null;
        RefinedPlaylist expectedPlaylist = DownstreamMapper.mapPlaylist(playlist);
        playlistDao.saveEntryToDatabase(expectedPlaylist);
        Object playlistObject = playlistDao.retrieveEntryById("2CTdEa3JWbncC1h8WjnuxZ");
        RefinedPlaylist actualPlaylist = toPlaylist((Document) playlistObject);
        Assert.assertEquals(expectedPlaylist, actualPlaylist);
    }

    @Test
    public void shouldPullAndSaveTrack() throws Exception {
        Track track = SpotifyTrackDao.pullTrack(api, "2g9txG7vtGj8lNuNLDxQhu");
        assert track != null;
        RefinedTrack expectedTrack = DownstreamMapper.mapTrack(track);
        trackDao.saveEntryToDatabase(expectedTrack);
        Object trackObject = trackDao.retrieveEntryById("2g9txG7vtGj8lNuNLDxQhu");
        RefinedTrack actualSong = toTrack((Document) trackObject);
        Assert.assertEquals(expectedTrack, actualSong);

    }

    @Test
    public void shouldPullAndSaveUser() throws Exception {
        User user = SpotifyUserDao.pullUser(api, "spotify");
        assert user != null;
        RefinedUser expectedUser = DownstreamMapper.mapUser(user);
        userDao.saveEntryToDatabase(expectedUser);
        Object userObject = userDao.retrieveEntryById("spotify");
        RefinedUser actualUser = toUser((Document) userObject);
        Assert.assertEquals(expectedUser, actualUser);

    }

}
