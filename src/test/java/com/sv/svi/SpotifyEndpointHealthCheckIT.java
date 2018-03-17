package com.sv.svi;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.requests.data.albums.GetAlbumRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@PropertySource("classpath:application.properties")
public class SpotifyEndpointHealthCheckIT {

    @Value("${clientId}")
    String clientId;
    @Value("${clientSecret}")
    String clientSecret;

    @Autowired
    SpotifyAuthenticationHelper authenticationHelper;

    private SpotifyApi api;


    @Before
    public void beforeEach() {
        this.authenticationHelper = new SpotifyAuthenticationHelper(clientId, clientSecret);
        this.api = authenticationHelper.clientCredentialFlow();
    }

    @Test
    public void shouldPullArtistFromSpotify() {
        final GetArtistRequest request = api.getArtist("7dGJo4pcD2V6oG8kP0tJRR").build();
        try {
            assert (request.execute() != null);
        } catch (Exception e) {
            log.error("Failed to download metadata for artist : 7dGJo4pcD2V6oG8kP0tJRR, HTTP error code: {} ", e.getMessage());
        }
    }

    @Test
    public void shouldPullAlbumFromSpotify() {
        final GetAlbumRequest request = api.getAlbum("73nS1mA0WtUzBHzFhG3D5g").build();
        try {
            assert (request.execute() != null);
        } catch (Exception e) {
            log.error("Failed to download metadata for album : 73nS1mA0WtUzBHzFhG3D5g, HTTP error code: {}", e.getMessage());
        }

    }

    @Test
    public void shouldPullTrackFromSpotify() {
        final GetTrackRequest request = api.getTrack("y1p3nME3c0YU0a8vHPi4Z").build();
        try {
            assert (request.execute() != null);
        } catch (Exception e) {
            log.error("Failed to download metadata for track : y1p3nME3c0YU0a8vHPi4Z, HTTP error code: {}", e.getMessage());
        }
    }

    @Test
    public void shouldPullPlaylistFromPlaylist(){
        final GetPlaylistRequest request = api.getPlaylist("spotifycharts","37i9dQZEVXbLnolsZ8PSNw").build();
        try {
            assert (request.execute() != null);
        } catch (Exception e) {
            log.error("Failed to download metadata for playlist : 37i9dQZEVXbLnolsZ8PSNw, HTTP error code: {}", e.getMessage());
        }
    }
}
