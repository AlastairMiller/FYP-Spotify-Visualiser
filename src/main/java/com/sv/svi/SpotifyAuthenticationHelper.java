package com.sv.svi;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Component
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
            log.info("Expires in: {}", clientCredentials.getExpiresIn());
        } catch (InterruptedException | ExecutionException e) {
         log.error(e.getCause().getMessage());
        }
        return api;
    }
}
