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
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class RefreshDbService {

    //TODO 1. Catch when access token expires

    private SpotifyAuthenticationHelper authenticationHelper;

    private MongoHelper mongoHelper;
    private PlaylistDto playlistDto;
    private ExecutorService executor = Executors.newFixedThreadPool(16);
    private SpotifyApi api;
    private Boolean apiSet = false;

    @Autowired
    public RefreshDbService(MongoHelper mongoHelper, SpotifyAuthenticationHelper authenticationHelper) {
        this.mongoHelper = mongoHelper;
        this.authenticationHelper = authenticationHelper;
    }

    @Scheduled(fixedDelay = 3600000)
    public void refreshToken() {
        executor.submit(() -> {
            log.info("-----------------------Refreshing Api Token-----------------------");
            api =  authenticationHelper.clientCredentialFlow();
            apiSet = true;
        });

    }


    //TODO Add endpoint when scheduler is implemented to initiate this
    @Scheduled(fixedDelay = 43200)
    public void main() {
        if(apiSet) {
            playlistDto = new PlaylistDto();
            log.info("-----------------------Database refresh started-----------------------");
            pullLocationPlaylists(api);
            pullFeaturedPlaylists(api);
        }else {
            try {
                Thread.sleep(300);
                main();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void pullLocationPlaylists(SpotifyApi api) {
        log.info("-----------------------Pulling location playlist's-----------------------");
        int playlistRetrieval = 0;
        List<PlaylistSimplified> playlists = playlistDto.pullPlaylistsForAUser(api, "thesoundsofspotify", playlistRetrieval);
        playlists.removeIf(SimplePlaylist -> !SimplePlaylist.getName().matches(".* [A-Z][A-Z]\\b"));
        try {
            playlistDto.saveSimplePlaylistsToMongo(api, playlists, mongoHelper.getMongoDao());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        log.info("{} playlists in the database", mongoHelper.getMongoDao().getCollection("RefinedPlaylists").count());
    }

    private void pullFeaturedPlaylists(SpotifyApi api) {
        log.info("-----------------------Pulling featured playlist's-----------------------");
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
            featuredPlaylists = request.execute();
            List<PlaylistSimplified> featuredPlaylistList = Arrays.asList(featuredPlaylists.getPlaylists().getItems());
            playlistDto.saveSimplePlaylistsToMongo(api, featuredPlaylistList, mongoHelper.getMongoDao());
        } catch (Exception e) {
            log.error("Failed to pull featured Playlists from Spotify Api, HTTP Status code: {}", e.getMessage());
        }
    }


}
