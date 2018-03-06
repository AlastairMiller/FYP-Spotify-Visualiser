package com.sps.utilities;

import com.sps.model.RefinedArtist;
import com.sps.model.RefinedPlaylist;
import com.sps.model.RefinedTrack;
import com.sps.model.RefinedUser;
import com.wrapper.spotify.model_objects.specification.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DownstreamMapper {

    public static RefinedTrack mapSong(Track fullTrack) {
        List<RefinedArtist> minimisedRefinedArtists = new ArrayList<RefinedArtist>();
        for (int i = 0; i < fullTrack.getArtists().length; i++) {
            minimisedRefinedArtists.add(mapSimpleArtist(fullTrack.getArtists()[i]));
        }
        try {
            return RefinedTrack.builder()
                    .id(fullTrack.getId())
                    .refinedArtists(minimisedRefinedArtists)
                    .availableMarkets(Arrays.asList(fullTrack.getAvailableMarkets().toString().split(",")))
                    .discNum(fullTrack.getDiscNumber())
                    .durationMs(fullTrack.getDurationMs())
                    .explicit(fullTrack.getIsExplicit())
                    .externalURL((new URL(fullTrack.getExternalUrls().get("spotify"))))
                    .href(fullTrack.getHref())
                    .name(fullTrack.getName())
                    .previewURL(new URL(fullTrack.getPreviewUrl()))
                    .trackNumber(fullTrack.getTrackNumber())
                    .popularity(fullTrack.getPopularity())
                    .uri(URI.create(fullTrack.getUri()))
                    .build();
        } catch (MalformedURLException e) {
            return RefinedTrack.builder()
                    .id(fullTrack.getId())
                    .refinedArtists(minimisedRefinedArtists)
                    .availableMarkets(Arrays.asList(fullTrack.getAvailableMarkets().toString().split(",")))
                    .discNum(fullTrack.getDiscNumber())
                    .durationMs(fullTrack.getDurationMs())
                    .explicit(fullTrack.getIsExplicit())
                    .href(fullTrack.getHref())
                    .name(fullTrack.getName())
                    .trackNumber(fullTrack.getTrackNumber())
                    .popularity(fullTrack.getPopularity())
                    .uri(URI.create(fullTrack.getUri()))
                    .build();
        }
    }

    public static RefinedArtist mapSimpleArtist(ArtistSimplified fullArtist) {
        try {
            return RefinedArtist.builder()
                    .id(fullArtist.getId())
                    .externalURL((new URL(fullArtist.getExternalUrls().get("spotify"))))
                    .href(fullArtist.getHref())
                    .name(fullArtist.getName())
                    .type(fullArtist.getType().getType())
                    .uri(URI.create(fullArtist.getUri()))
                    .build();
        } catch (MalformedURLException e) {
            return RefinedArtist.builder()
                    .id(fullArtist.getId())
                    .href(fullArtist.getHref())
                    .name(fullArtist.getName())
                    .type(fullArtist.getType().getType())
                    .uri(URI.create(fullArtist.getUri()))
                    .build();
        }
    }

    public static RefinedArtist mapArtist(Artist fullArtist) {
        try {
            return RefinedArtist.builder()
                    .id(fullArtist.getId())
                    .externalURL((new URL(fullArtist.getExternalUrls().get("spotify"))))
                    .genres(Arrays.asList(fullArtist.getGenres()))
                    .href(fullArtist.getHref())
                    .name(fullArtist.getName())
                    .type(fullArtist.getType().getType())
                    .uri(URI.create(fullArtist.getUri()))
                    .followers(fullArtist.getFollowers().getTotal())
                    .popularity(fullArtist.getPopularity())
                    .build();
        } catch (MalformedURLException e) {
            return RefinedArtist.builder()
                    .id(fullArtist.getId())
                    .genres(Arrays.asList(fullArtist.getGenres()))
                    .href(fullArtist.getHref())
                    .name(fullArtist.getName())
                    .type(fullArtist.getType().getType())
                    .uri(URI.create(fullArtist.getUri()))
                    .followers(fullArtist.getFollowers().getTotal())
                    .popularity(fullArtist.getPopularity())
                    .build();
        }
    }

    public static RefinedUser mapUser(User fullUser) {
        List<String> imageUrls = new ArrayList<String>();
        if( fullUser.getImages()!=null) {
            for (int i = 0; i < fullUser.getImages().length; i++) {
                imageUrls.add(fullUser.getImages()[i].getUrl());
            }
        }
        return RefinedUser.builder()
                .id(fullUser.getId())
                .uri(URI.create(fullUser.getUri()))
                .displayName(fullUser.getDisplayName())
                //Possible bug in wrapper not returning followers / not sufficient permissions? .numOfFollowers(fullUser.getFollowers().getTotal())
                .href(fullUser.getHref())
                .images(imageUrls)
                .build();
    }

    public static RefinedPlaylist mapPlaylist(Playlist fullPlaylist){
        List<String> imageUrls = new ArrayList<>();
        if(fullPlaylist.getImages()!=null) {
            for (int i = 0; i < fullPlaylist.getImages().length; i++) {
                imageUrls.add(fullPlaylist.getImages()[i].getUrl());
            }
        }
        List<RefinedTrack> formattedRefinedTracks = new ArrayList<RefinedTrack>();
        for (int n = 0; n < fullPlaylist.getTracks().getItems().length; n++) {
            formattedRefinedTracks.add(mapSong(fullPlaylist.getTracks().getItems()[n].getTrack()));
        }
        try {
            return RefinedPlaylist.builder()
                    .id(fullPlaylist.getId())
                    .uri(URI.create(fullPlaylist.getUri()))
                    .externalURL(new URL(fullPlaylist.getExternalUrls().get("spotify")))
                    .numOfFollowers(fullPlaylist.getFollowers().getTotal())
                    .href(fullPlaylist.getHref())
                    .images(imageUrls)
                    .name(fullPlaylist.getName())
                    .refinedUser(mapUser(fullPlaylist.getOwner()))
                    .refinedTracks(formattedRefinedTracks)
                    .build();
        } catch (MalformedURLException e) {
            return RefinedPlaylist.builder()
                    .id(fullPlaylist.getId())
                    .uri(URI.create(fullPlaylist.getUri()))
                    .numOfFollowers(fullPlaylist.getFollowers().getTotal())
                    .href(fullPlaylist.getHref())
                    .images(imageUrls)
                    .name(fullPlaylist.getName())
                    .refinedUser(mapUser(fullPlaylist.getOwner()))
                    .refinedTracks(formattedRefinedTracks)
                    .build();
        }

    }


}
