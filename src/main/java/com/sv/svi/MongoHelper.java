package com.sv.svi;

import com.svd.ClientHandler;
import com.svd.dao.MongoDao;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import refinedDataModels.*;

@Slf4j
@Component
@Getter
public class MongoHelper {

    private final MongoDao<RefinedAlbum> refinedAlbumDao;
    private final MongoDao<RefinedArtist> refinedArtistDao;
    private final MongoDao<RefinedPlaylist> refinedPlaylistDao;
    private final MongoDao<RefinedTrack> refinedTrackDao;
    private final MongoDao<RefinedUser> refinedUserDao;


    public MongoHelper(@Value("${mongo.hostname:localhost}") String hostname, @Value("${mongo.port:27017}") int port,
                       @Value("{$mongo.username:mongo}") String username, @Value("${mongo.dbname:spotify}") String dbname, @Value("${mongo.password:mongo}") String password) {
        refinedArtistDao = new MongoDao<>(new ClientHandler(hostname, port, username, dbname, password.toCharArray()), "RefinedArtists", RefinedArtist.class);
        refinedAlbumDao = new MongoDao<>(new ClientHandler(hostname, port, username, dbname, password.toCharArray()), "RefinedAlbums", RefinedAlbum.class);
        refinedPlaylistDao = new MongoDao<>(new ClientHandler(hostname, port, username, dbname, password.toCharArray()), "RefinedPlaylists", RefinedPlaylist.class);
        refinedTrackDao = new MongoDao<>(new ClientHandler(hostname, port, username, dbname, password.toCharArray()), "RefinedTracks", RefinedTrack.class);
        refinedUserDao = new MongoDao<>(new ClientHandler(hostname, port, username, dbname, password.toCharArray()), "RefinedUsers", RefinedUser.class);
    }
}
