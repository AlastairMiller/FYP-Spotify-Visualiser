package al.miller.sv.ingest;

import al.miller.sv.ds.ClientHandler;
import al.miller.sv.ds.dao.*;
import al.miller.sv.ingest.dao.*;
import al.miller.sv.ingest.utils.DownstreamMapper;
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

@Slf4j
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@PropertySource("classpath:application.properties")
public class ApplicationE2ETest {

    @Autowired
    private SpotifyAuthenticationHelper authenticationHelper;

    private SpotifyApi api;
    private MongodExecutable mongodExe;
    private MongodProcess mongod;
    private RefinedAlbumDao refinedAlbumDao;
    private RefinedArtistDao refinedArtistDao;
    private RefinedPlaylistDao refinedPlaylistDao;
    private RefinedTrackDao refinedTrackDao;
    private RefinedUserDao refinedUserDao;
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
        refinedAlbumDao = new RefinedAlbumDao(new ClientHandler(hostname, port, "embedded"), "RefinedAlbums");
        refinedArtistDao = new RefinedArtistDao(new ClientHandler(hostname, port, "embedded"), "RefinedArtists");
        refinedPlaylistDao = new RefinedPlaylistDao(new ClientHandler(hostname, port, "embedded"), "RefinedPlaylists");
        refinedTrackDao = new RefinedTrackDao(new ClientHandler(hostname, port, "embedded"), "RefinedTracks");
        refinedUserDao = new RefinedUserDao(new ClientHandler(hostname, port, "embedded"), "RefinedUsers");
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
        refinedAlbumDao.save(expectedAlbum);
        RefinedAlbum refinedAlbum = refinedAlbumDao.getById("0gtU58GVllHFpZzIgLd5lV");
        Assert.assertEquals(expectedAlbum, refinedAlbum);
    }

    @Test
    public void shouldPullAndSaveArtist() throws Exception {
        Artist artist = SpotifyArtistDao.pullArtist(api, "5WId4o5jdGVhptNU0uqKxu");
        assert artist != null;
        RefinedArtist expectedArtist = DownstreamMapper.mapArtist(artist);
        refinedArtistDao.save(expectedArtist);
        RefinedArtist refinedArtist= refinedArtistDao.getById("5WId4o5jdGVhptNU0uqKxu");
        Assert.assertEquals(expectedArtist, refinedArtist);
    }

    @Test
    public void shouldPullAndSavePlaylist() throws Exception {
        Playlist playlist = SpotifyPlaylistDao.pullPlaylistSync(api, "millersinc", "2CTdEa3JWbncC1h8WjnuxZ");
        assert playlist != null;
        RefinedPlaylist expectedPlaylist = DownstreamMapper.mapPlaylist(playlist);
        refinedPlaylistDao.save(expectedPlaylist);
        RefinedPlaylist playlistFromDB = refinedPlaylistDao.getById("2CTdEa3JWbncC1h8WjnuxZ");
        Assert.assertEquals(expectedPlaylist, playlistFromDB);
    }

    @Test
    public void shouldPullAndSaveTrack() throws Exception {
        Track track = SpotifyTrackDao.pullTrack(api, "2g9txG7vtGj8lNuNLDxQhu");
        assert track != null;
        RefinedTrack expectedTrack = DownstreamMapper.mapTrack(track);
        refinedTrackDao.save(expectedTrack);
        RefinedTrack trackFromDB = refinedTrackDao.getById("2g9txG7vtGj8lNuNLDxQhu");
        Assert.assertEquals(expectedTrack, trackFromDB);

    }

    @Test
    public void shouldPullAndSaveUser() throws Exception {
        User user = SpotifyUserDao.pullUser(api, "spotify");
        assert user != null;
        RefinedUser expectedUser = DownstreamMapper.mapUser(user);
        refinedUserDao.save(expectedUser);
        RefinedUser userFromDB = refinedUserDao.getById("spotify");
        Assert.assertEquals(expectedUser, userFromDB);

    }

}
