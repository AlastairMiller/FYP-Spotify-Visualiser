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
import sun.nio.ch.SelectorImpl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
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
        playlistDto = new PlaylistDto();
        log.info("-----------------------Database refresh started-----------------------");
        pullLocationPlaylists(api);
        pullFeaturedPlaylists(api);
    }

    private void pullLocationPlaylists(SpotifyApi api) {
        log.info("-----------------------Pulling location playlist's-----------------------");
        int playlistRetrieval = 0;
        List<PlaylistSimplified> playlists = playlistDto.pullPlaylistsForAUser(api, "thesoundsofspotify", playlistRetrieval);
        playlists.removeIf(SimplePlaylist -> !SimplePlaylist.getName().contains("The Sound of "));
        playlists.removeIf(SimplePlaylist -> SimplePlaylist.getName().matches(".* [A-Z][A-Z]\\b"));
        playlists.subList(100,playlists.size()).clear();
        playlistDto.saveSimplePlaylistsToMongo(api, playlists, mongoHelper.getMongoDao());
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
            //Todo async
            featuredPlaylists = request.execute();
            List<PlaylistSimplified> featuredPlaylistList = Arrays.asList(featuredPlaylists.getPlaylists().getItems());
            playlistDto.saveSimplePlaylistsToMongo(api, featuredPlaylistList, mongoHelper.getMongoDao());
        } catch (Exception e) {
            log.error("Failed to pull featured Playlists from Spotify Api, HTTP Status code: {}", e.getMessage());
        }
    }


}
