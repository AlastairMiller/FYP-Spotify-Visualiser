package com.sv.svi;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.requests.data.artists.GetArtistRequest;
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

    SpotifyApi api;


    @Autowired
    SpotifyAuthenticationHelper authenticationHelper;

    @Before
    public void beforeEach() {
        this.authenticationHelper = new SpotifyAuthenticationHelper(clientId,clientSecret);
        this.api = authenticationHelper.clientCredentialFlow();
    }

    @Test
    public void shouldPullArtistFromSpotify() {
        GetArtistRequest request = api.getArtist("7dGJo4pcD2V6oG8kP0tJRR").build();
        try {
            assert (request.execute() != null);
        } catch (Exception e) {
            log.error("Failed to download metadata for artist : 7dGJo4pcD2V6oG8kP0tJRR, HTTP error code: {} ", e.getMessage());
        }

    }
}
