package al.miller.sv.ingest;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Configuration
@Component
@PropertySource("classpath:application.properties")
public class SpotifyAuthenticationHelper {
    private final String clientId;
    private final String clientSecret;

    @Autowired
    public SpotifyAuthenticationHelper(@Value("${clientId}") String clientId, @Value("${clientSecret}") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public SpotifyApi clientCredentialFlow() {
        final SpotifyApi api = new SpotifyApi.Builder()
                .setClientId(this.clientId)
                .setClientSecret(this.clientSecret)
                .build();

        final ClientCredentialsRequest request = api.clientCredentials().build();

        try {
            final Future<ClientCredentials> responseFuture = request.executeAsync();
            final ClientCredentials clientCredentials = responseFuture.get();
            api.setAccessToken(clientCredentials.getAccessToken());
            log.info("Token expires in: {}", clientCredentials.getExpiresIn());
        } catch (InterruptedException | ExecutionException e) {
         log.error(e.getCause().getMessage());
        }
        return api;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
