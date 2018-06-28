package al.miller.sv.ingest;

import com.svd.ClientHandler;
import com.svd.dao.*;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@Getter
@PropertySource("classpath:application.properties")
public class MongoHelper {

    private final PlaylistSimplifiedDao playlistSimplifiedDao;
    private final RefinedAlbumDao refinedAlbumDao;
    private final RefinedArtistDao refinedArtistDao;
    private final RefinedPlaylistDao refinedPlaylistDao;
    private final RefinedTrackDao refinedTrackDao;
    private final RefinedUserDao refinedUserDao;


    public MongoHelper(@Value("${mongo.hostname:localhost}") String hostname, @Value("${mongo.port:27017}") int port,
                       @Value("${mongo.username:mongo}") String username, @Value("${mongo.dbname:spotify}") String dbname, @Value("${mongo.password:mongo}") String password) {
        playlistSimplifiedDao = new PlaylistSimplifiedDao(new ClientHandler(hostname, port, dbname), "PlaylistSimplified");
        refinedArtistDao = new RefinedArtistDao(new ClientHandler(hostname, port,  dbname), "RefinedArtists");
        refinedAlbumDao = new RefinedAlbumDao(new ClientHandler(hostname, port,  dbname), "RefinedAlbums");
        refinedPlaylistDao = new RefinedPlaylistDao(new ClientHandler(hostname, port,  dbname), "RefinedPlaylists");
        refinedTrackDao = new RefinedTrackDao(new ClientHandler(hostname, port,  dbname), "RefinedTracks");
        refinedUserDao = new RefinedUserDao(new ClientHandler(hostname, port,  dbname), "RefinedUsers");
    }
}
