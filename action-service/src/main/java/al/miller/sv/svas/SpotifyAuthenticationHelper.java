package al.miller.sv.svas;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

@Slf4j
@Configuration
@Component
@PropertySource("classpath:application.properties")
@Data
public class SpotifyAuthenticationHelper {
    private final String clientId;
    private final String clientSecret;
    private final URI redirectUri;
    private final String state;
    private Integer timeToExpiry = 0;

    @Autowired
    public SpotifyAuthenticationHelper(@Value("${clientId}") String clientId, @Value("${clientSecret}") String clientSecret, @Value("${redirectURI}") String redirectUri, @Value("$state") String state) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = URI.create(redirectUri);
        this.state = state;
    }

    public SpotifyApi setupUnAuthApi() {
        SpotifyApi api = new SpotifyApi.Builder()
                .setClientId(this.clientId)
                .setClientSecret(this.clientSecret)
                .setRedirectUri(this.redirectUri)
                .build();
        return api;
    }

    public SpotifyApi authorisationCodeFlow(SpotifyApi api, String code) {

        final AuthorizationCodeRequest authorizationCodeRequest = api.authorizationCode(code)
                .build();

        try {
            final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();
            log.info("" + authorizationCodeCredentials.getExpiresIn());
            timeToExpiry = authorizationCodeCredentials.getExpiresIn();
            api = new SpotifyApi.Builder()
                    .setClientId(this.clientId)
                    .setClientSecret(this.clientSecret)
                    .setRedirectUri(this.redirectUri)
                    .setAccessToken(authorizationCodeCredentials.getAccessToken())
                    .setRefreshToken(authorizationCodeCredentials.getRefreshToken())
                    .build();
            return api;
        } catch (IOException | SpotifyWebApiException e) {
            log.error(e.getMessage());
        }
        return api;
    }

    public URI generateAuthUri(SpotifyApi api) {
        final AuthorizationCodeUriRequest authorizationCodeUriRequest = api.authorizationCodeUri()
                .state(this.state)
                .scope("playlist-read-private,user-read-recently-played,user-top-read,user-read-private")
                .show_dialog(false)
                .build();
        return authorizationCodeUriRequest.execute();
    }


    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
