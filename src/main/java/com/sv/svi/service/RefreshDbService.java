package com.sv.svi.service;

import com.neovisionaries.i18n.CountryCode;
import com.sv.svi.MongoHelper;
import com.sv.svi.SpotifyAuthenticationHelper;
import com.sv.svi.dto.PlaylistDto;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.special.FeaturedPlaylists;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.requests.data.browse.GetListOfFeaturedPlaylistsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class RefreshDbService {

    //TODO 1. Catch when access token expires
    //TODO 3. Write UI
    //TODO 4. Write user comparision service/module
    //TODO 5. Write documentation
    //TODO 6. Find someplace to host

    private SpotifyAuthenticationHelper authenticationHelper;

    private MongoHelper mongoHelper;
    private PlaylistDto playlistDto;

    @Autowired
    public RefreshDbService(MongoHelper mongoHelper, SpotifyAuthenticationHelper authenticationHelper) {
        this.mongoHelper = mongoHelper;
        this.authenticationHelper = authenticationHelper;
    }


    //TODO Add endpoint when scheduler is implemented to initiate this
    @Scheduled(fixedDelay = 43200)
    public void main() {
        SpotifyApi api = authenticationHelper.clientCredentialFlow();
        log.info("-----------------------Database refresh started-----------------------");
        pullLocationPlaylists(api);
        pullFeaturedPlaylists(api);
    }

    private void pullLocationPlaylists(SpotifyApi api) {
        log.info("-----------------------Pulling Location Playlists-----------------------");
        List<PlaylistSimplified> playlists = playlistDto.pullPlaylistsForAUser(api, "thesoundsofspotify", 0);
        playlists.removeIf(SimplePlaylist -> !SimplePlaylist.getName().contains("The Sound of "));
        playlists.removeIf(SimplePlaylist -> SimplePlaylist.getName().matches("\\w*[A-Z][A-Z]\\b"));
        for (int i = 0; i < playlists.size(); i++) {
            if (playlists.get(i).getName().matches(".*[A-Z][A-Z]\\b")) {
                playlists.remove(i);
            }
        }
        playlistDto.saveSimplePlaylistsToDatastore(api, playlists);
    }

    private void pullFeaturedPlaylists(SpotifyApi api) {
        log.info("-----------------------Pulling featured Playlists-----------------------");
        Date timestamp = new Date();
        FeaturedPlaylists featuredPlaylists;

        final GetListOfFeaturedPlaylistsRequest request = api.getListOfFeaturedPlaylists()
                .limit(20)
                .offset(1)
                .country(CountryCode.GB)
                .offset(0)
                .timestamp(timestamp)
                .build();
        try {
            //Todo async
            featuredPlaylists = request.execute();
            List<PlaylistSimplified> featuredPlaylistList = Arrays.asList(featuredPlaylists.getPlaylists().getItems());
            playlistDto.saveSimplePlaylistsToDatastore(api, featuredPlaylistList);
        } catch (Exception e) {
            log.error("Failed to pull featured Playlists from Spotify Api, HTTP Status code: {}", e.getMessage());
        }
    }


}
