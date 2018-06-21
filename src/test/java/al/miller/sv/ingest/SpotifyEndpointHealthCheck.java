package al.miller.sv.ingest;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
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

import java.io.IOException;

@Slf4j
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@PropertySource("classpath:application.properties")
public class SpotifyEndpointHealthCheck {

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
    public void shouldPullArtistFromSpotify() throws IOException, SpotifyWebApiException {
        final GetArtistRequest request = api.getArtist("7dGJo4pcD2V6oG8kP0tJRR").build();
        assert (request.execute() != null);

    }

    @Test
    public void shouldPullAlbumFromSpotify() throws IOException, SpotifyWebApiException {
        final GetAlbumRequest request = api.getAlbum("73nS1mA0WtUzBHzFhG3D5g").build();
        assert (request.execute() != null);
    }

    @Test
    public void shouldPullTrackFromSpotify() throws IOException, SpotifyWebApiException {
        final GetTrackRequest request = api.getTrack("y1p3nME3c0YU0a8vHPi4Z").build();
        assert (request.execute() != null);
    }

    @Test
    public void shouldPullPlaylistFromPlaylist() throws IOException, SpotifyWebApiException {
        final GetPlaylistRequest request = api.getPlaylist("spotifycharts", "37i9dQZEVXbLnolsZ8PSNw").build();
        assert (request.execute() != null);

    }
}
