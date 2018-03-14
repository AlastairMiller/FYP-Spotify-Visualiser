package com.sv.sps.utilities;

import com.wrapper.spotify.model_objects.specification.*;
import lombok.extern.slf4j.Slf4j;
import refinedDataModels.RefinedArtist;
import refinedDataModels.RefinedPlaylist;
import refinedDataModels.RefinedTrack;
import refinedDataModels.RefinedUser;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class DownstreamMapper {

    public static RefinedTrack mapTrack(Track fullTrack) {
        List<String> artistIds = new ArrayList<>();
        List<String> albumIds  = new ArrayList<>();
//        for (int i = 0; i < fullTrack.getArtists().length; i++) {
//            minimisedRefinedArtists.add(mapSimpleArtist(fullTrack.getArtists()[i]));
//        }
        try {
            return RefinedTrack.builder()
                    .id(fullTrack.getId())
                    .refinedAlbumIds(albumIds)
                    .refinedArtistIds(artistIds)
                    .discNum(fullTrack.getDiscNumber())
                    .durationMs(fullTrack.getDurationMs())
                    .explicit(fullTrack.getIsExplicit())
                    .externalURL((new URL(fullTrack.getExternalUrls().get("spotify"))))
                    .href(new URL(fullTrack.getHref()))
                    .name(fullTrack.getName())
                    .previewURL(new URL(fullTrack.getPreviewUrl()))
                    .trackNumber(fullTrack.getTrackNumber())
                    .popularity(fullTrack.getPopularity())
                    .uri(URI.create(fullTrack.getUri()))
                    .build();
        } catch (MalformedURLException e) {
            log.error("Either the track's externalUrl: {}, href: {} or previewUrl: {} is invalid",
                    fullTrack.getExternalUrls().get("spotify"), fullTrack.getHref(), fullTrack.getPreviewUrl());
            return RefinedTrack.builder()
                    .id(fullTrack.getId())
                    .refinedArtistIds(artistIds)
                    .discNum(fullTrack.getDiscNumber())
                    .durationMs(fullTrack.getDurationMs())
                    .explicit(fullTrack.getIsExplicit())
                    .name(fullTrack.getName())
                    .trackNumber(fullTrack.getTrackNumber())
                    .popularity(fullTrack.getPopularity())
                    .uri(URI.create(fullTrack.getUri()))
                    .build();
        }
    }

    public static RefinedArtist mapArtist(Artist fullArtist) {
        List<String> albumIds  = new ArrayList<>();
        try {
            return RefinedArtist.builder()
                    .id(fullArtist.getId())
                    .externalURL((new URL(fullArtist.getExternalUrls().get("spotify"))))
                    .genres(Arrays.asList(fullArtist.getGenres()))
                    .refinedAlbumIds(albumIds)
                    .href(new URL(fullArtist.getHref()))
                    .name(fullArtist.getName())
                    .followers(fullArtist.getFollowers().getTotal())
                    .popularity(fullArtist.getPopularity())
                    .uri(URI.create(fullArtist.getUri()))
                    .build();
        } catch (MalformedURLException e) {
            log.error("Either the track's externalUrl: {} or href: {} is invalid",
                    fullArtist.getExternalUrls().get("spotify"), fullArtist.getHref());
            return RefinedArtist.builder()
                    .id(fullArtist.getId())
                    .genres(Arrays.asList(fullArtist.getGenres()))
                    .refinedAlbumIds(albumIds)
                    .name(fullArtist.getName())
                    .followers(fullArtist.getFollowers().getTotal())
                    .popularity(fullArtist.getPopularity())
                    .uri(URI.create(fullArtist.getUri()))
                    .build();
        }
    }

    public static RefinedUser mapUser(User fullUser) {
        try {
            return RefinedUser.builder()
                    .id(fullUser.getId())
                    .displayName(fullUser.getDisplayName())
                    .externalURL(new URL(fullUser.getExternalUrls().get("spotify")))
                    //Possible bug in wrapper not returning followers / not sufficient permissions?
                    .numOfFollowers(fullUser.getFollowers().getTotal())
                    .href(new URL(fullUser.getHref()))
                    .imageURL(new URL(fullUser.getImages()[0].getUrl()))
                    .uri(URI.create(fullUser.getUri()))
                    .build();
        } catch (MalformedURLException e) {
            log.error("Either the user's externalUrl: {}, href: {} or image URL {} is invalid ",
                    fullUser.getExternalUrls().get("spotify"), fullUser.getHref(), fullUser.getImages()[0].getUrl());
            return RefinedUser.builder()
                    .id(fullUser.getId())
                    .displayName(fullUser.getDisplayName())
                    //Possible bug in wrapper not returning followers / not sufficient permissions?
                    .numOfFollowers(fullUser.getFollowers().getTotal())
                    .uri(URI.create(fullUser.getUri()))
                    .build();
        }
    }

    public static RefinedPlaylist mapPlaylist(Playlist fullPlaylist){
        List<String> trackIds = new ArrayList<>();
        try {
            return RefinedPlaylist.builder()
                    .id(fullPlaylist.getId())
                    .externalURL(new URL(fullPlaylist.getExternalUrls().get("spotify")))
                    .numOfFollowers(fullPlaylist.getFollowers().getTotal())
                    .href(new URL(fullPlaylist.getHref()))
                    .imageURL(new URL(fullPlaylist.getImages()[0].getUrl()))
                    .name(fullPlaylist.getName())
                    .refinedUserId(fullPlaylist.getOwner().getId())
                    .refinedTrackIds(trackIds)
                    .uri(URI.create(fullPlaylist.getUri()))
                    .build();
        } catch (MalformedURLException e) {
            log.error("Either the playlist's externalUrl: {}, href: {} or image URL {} is invalid ",
                    fullPlaylist.getExternalUrls().get("spotify"), fullPlaylist.getHref(), fullPlaylist.getImages()[0].getUrl());
            return RefinedPlaylist.builder()
                    .id(fullPlaylist.getId())
                    .numOfFollowers(fullPlaylist.getFollowers().getTotal())
                    .name(fullPlaylist.getName())
                    .refinedUserId(fullPlaylist.getOwner().getId())
                    .refinedTrackIds(trackIds)
                    .uri(URI.create(fullPlaylist.getUri()))
                    .build();
        }

    }


}
