package al.miller.sv.svas.service;

import al.miller.sv.svas.SpotifyAuthenticationHelper;
import al.miller.sv.svas.dao.AuthorisedUserDao;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.model_objects.specification.Recommendations;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Data
public class UserService {

    private final SpotifyAuthenticationHelper authenticationHelper;
    private SpotifyApi spotifyApi;
    private String code;
    private LocalDateTime timeTillRefresh;


    @Autowired
    public UserService(SpotifyAuthenticationHelper authenticationHelper) {
        this.authenticationHelper = authenticationHelper;
        this.spotifyApi = authenticationHelper.setupUnAuthApi();
        timeTillRefresh = LocalDateTime.MIN;
    }

    public void setCodeAndAuthAPI(String code) {
        this.code = code;
        this.spotifyApi = authenticationHelper.authorisationCodeFlow(spotifyApi, code);
        timeTillRefresh = LocalDateTime.now().plusSeconds(authenticationHelper.getTimeToExpiry());
    }

    public List<PlaylistSimplified> getPlaylists(String userId) {
        if (LocalDateTime.now().isAfter(timeTillRefresh)) {
            authorizationCodeRefresh();
        }
        return AuthorisedUserDao.pullUserPlaylists(spotifyApi, userId);
    }

    public List<Artist> getArtists() {
        if (LocalDateTime.now().isAfter(timeTillRefresh)) {
            authorizationCodeRefresh();
        }
        return AuthorisedUserDao.getTopArtists(spotifyApi);
    }

    public List<Track> getTracks() {
        if (LocalDateTime.now().isAfter(timeTillRefresh)) {
            authorizationCodeRefresh();
        }
        return AuthorisedUserDao.getTopTracks(spotifyApi);
    }

    public Recommendations getRecommedations() {
        if (LocalDateTime.now().isAfter(timeTillRefresh)) {
            authorizationCodeRefresh();
        }
        return AuthorisedUserDao.recommedations(spotifyApi, getTracks());
    }

    public URI generateAuthUri() {
        return authenticationHelper.generateAuthUri(spotifyApi);
    }


    public void authorizationCodeRefresh() {
        AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
                .build();
        try {
            final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();

            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            log.info("Token refreshed, expires in {}", authorizationCodeCredentials.getExpiresIn());
            timeTillRefresh = LocalDateTime.now();
            timeTillRefresh = timeTillRefresh.plusSeconds(authorizationCodeCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException e) {
            log.error(e.getMessage());
        }

    }


}
