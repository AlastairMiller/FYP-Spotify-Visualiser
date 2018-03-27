package com.sv.svi.utils;

import com.google.gson.internal.LinkedTreeMap;
import com.svd.mapper.Builder;
import com.wrapper.spotify.model_objects.specification.*;
import lombok.extern.slf4j.Slf4j;
import refinedDataModels.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static com.svd.mapper.RefinedtoJsonMapper.invokeSimpleGetter;

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
                    .spotifyURI(fullAlbum.getUri())
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
                    .spotifyURI(fullAlbum.getUri())
                    .build();
        }
    }

    public static RefinedTrack mapTrack(Track fullTrack) {
        Map<String, Object> map = mapGeneric(fullTrack, Track.class);
        map.put("artists",((ArtistSimplified) map.get("artists")).getId());
        map.put("album",((ArtistSimplified) map.get("artists")).getId())));
map.put("album", map.forEach((k,v) -> if(k == "album"){ );)
        return (RefinedTrack) Builder.start(RefinedTrack.class).with(map).build();
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
                    .spotifyURI(fullArtist.getUri())
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
                    .spotifyURI(fullArtist.getUri())
                    .build();
        }
    }

    public static RefinedUser mapUser(User fullUser) {
        return (RefinedUser) Builder.start(RefinedUser.class).with(mapGeneric(fullUser, User.class)).build();

    }


    public static RefinedPlaylist mapPlaylist(Playlist fullPlaylist) {
        return (RefinedPlaylist) Builder.start(RefinedPlaylist.class).with(mapGeneric(fullPlaylist, Playlist.class)).build();
    }

    public static <T> Map<String, Object> mapGeneric(T instance, Class<T> clazz) {
        Map<String, Object> instanceMap = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Method getter;
                if (field.getType().getName().equals("boolean")) {
                    getter = clazz.getDeclaredMethod("is" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1));
                } else {
                    getter = clazz.getDeclaredMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1));
                }
                if (invokeSimpleGetter(getter, instance) != null)
                    instanceMap.put(field.getName(), invokeSimpleGetter(getter, instance));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        ExternalUrl externalUrl = (ExternalUrl) instanceMap.get("externalUrls");
        instanceMap.remove("externalIds");
        instanceMap.put("externalUrls", Arrays.asList(((ExternalUrl) instanceMap.get("externalUrls")).getExternalUrls()).get(0));
        return instanceMap;
    }

}
