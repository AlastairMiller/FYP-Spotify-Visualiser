package com.sv.svi;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.sv.svi.dto.AlbumDto;
import com.sv.svi.dto.PlaylistDto;
import com.sv.svi.dto.TrackDto;
import com.sv.svi.dto.UserDto;
import com.sv.svi.utils.DownstreamMapper;
import com.svd.ClientHandler;
import com.svd.dao.BaseDao;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.Track;
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
import refinedDataModels.RefinedTrack;

import java.io.IOException;

import static com.svd.mapper.JsontoRefinedMapper.toTrack;

@Slf4j
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@PropertySource("classpath:application.properties")
public class e2e {

    @Autowired
    private SpotifyAuthenticationHelper authenticationHelper;
    @Autowired
    private MongoHelper mongoHelper;
    private AlbumDto albumDto;
    private PlaylistDto playlistDto;
    private TrackDto trackDto;
    private UserDto userDto;
    private SpotifyApi api;

    private MongodExecutable mongodExe;
    private MongodProcess mongod;
    private BaseDao mongoDao;
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
        mongoDao = new BaseDao<>(new ClientHandler(hostname, port, "embedded"));
    }

    @After
    public void afterEach() {
        if (this.mongod != null) {
            this.mongod.stop();
            this.mongodExe.stop();
        }
    }

    @Test
    public void shouldPullAndSaveTrack(){
        Track track = TrackDto.pullTrack(api, "2g9txG7vtGj8lNuNLDxQhu");
        assert(track != null);
        RefinedTrack refinedTrack = DownstreamMapper.mapTrack(track);
        MongoCollection<Document> mongoCollection = mongoDao.getClientHandler().getMongoDB().getCollection("RefinedTracks");
        mongoDao.saveEntryToDatabase(RefinedTrack.class, refinedTrack, mongoCollection);
        Object object = mongoDao.retrieveEntryById("2g9txG7vtGj8lNuNLDxQhu", "RefinedTracks");
        RefinedTrack actualSong = toTrack((Document) object);
        Assert.assertEquals(refinedTrack, actualSong);

    }

}
