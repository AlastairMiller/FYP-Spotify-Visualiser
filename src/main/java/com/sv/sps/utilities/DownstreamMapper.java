package com.sv.sps.utilities;

import com.wrapper.spotify.model_objects.specification.*;
import lombok.extern.slf4j.Slf4j;
import refinedDataModels.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
public class DownstreamMapper {


    private static <T> List<String> returnNestedIds(Class<?> clazz, List<T> objects) {
        List<String> nestedIds = new ArrayList<>();
        for (T object : objects) {
            try {
                Field field = clazz.getDeclaredField("id");
                Method getter = clazz.getDeclaredMethod("get" + field.getName().substring(0, 1).toUpperCase() +
                        field.getName().substring(1));
                nestedIds.add(String.valueOf(getter.invoke(object)));
            } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return nestedIds;
    }


    public static RefinedAlbum mapAlbum(Album fullAlbum) {
        List<String> refinedArtists = returnNestedIds(ArtistSimplified.class, Arrays.asList(fullAlbum.getArtists()));
        List<String> tracksIds = returnNestedIds(TrackSimplified.class, Arrays.asList(fullAlbum.getTracks().getItems()));
        try {
            return RefinedAlbum.builder()
                    .id(fullAlbum.getId())
                    .name(fullAlbum.getName())
                    .artistsIds(refinedArtists)
                    .trackIds(tracksIds)
                    .imageURL(new URL(fullAlbum.getImages()[0].getUrl()))
                    .releaseDate(new Date(fullAlbum.getReleaseDate()))
                    .popularity(fullAlbum.getPopularity())
                    .externalURL(new URL(fullAlbum.getExternalUrls().get("spotify")))
                    .href(new URL(fullAlbum.getHref()))
                    .uri(URI.create(fullAlbum.getUri()))
                    .build();
        } catch (MalformedURLException e) {
            log.error("Either the album's externalUrl: {} or imageUrl: {} is invalid",
                    fullAlbum.getExternalUrls().get("spotify"), fullAlbum.getImages()[0].getUrl());
            return RefinedAlbum.builder()
                    .id(fullAlbum.getId())
                    .name(fullAlbum.getName())
                    .artistsIds(refinedArtists)
                    .trackIds(tracksIds)
                    .releaseDate(new Date(fullAlbum.getReleaseDate()))
                    .popularity(fullAlbum.getPopularity())
                    .uri(URI.create(fullAlbum.getUri()))
                    .build();
        }
    }

    public static RefinedTrack mapTrack(Track fullTrack) {
        List<String> artistIds = returnNestedIds(ArtistSimplified.class, Arrays.asList(fullTrack.getArtists()));
        List<String> albumIds = returnNestedIds(AlbumSimplified.class, Arrays.asList(fullTrack.getAlbum()));
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
        try {
            return RefinedArtist.builder()
                    .id(fullArtist.getId())
                    .externalURL((new URL(fullArtist.getExternalUrls().get("spotify"))))
                    .genres(Arrays.asList(fullArtist.getGenres()))
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

    public static RefinedPlaylist mapPlaylist(Playlist fullPlaylist) {
        List<String> trackIds = returnNestedIds(PlaylistTrack.class, Arrays.asList(fullPlaylist.getTracks().getItems()));
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
