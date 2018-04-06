package com.sv.svi.utils;

import com.svd.mapper.RefinedObjectBuilder;
import com.wrapper.spotify.model_objects.specification.*;
import lombok.extern.slf4j.Slf4j;
import refinedDataModels.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

import static com.svd.mapper.RefinedtoJsonMapper.invokeSimpleGetter;

@Slf4j
public class DownstreamMapper {
    private static List<String> listOfIds = new ArrayList<>();

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


    public static RefinedAlbum mapAlbum(Album fullAlbum) throws MalformedURLException {
        List<String> refinedArtists = returnNestedIds(ArtistSimplified.class, Arrays.asList(fullAlbum.getArtists()));
        List<String> tracksIds = returnNestedIds(TrackSimplified.class, Arrays.asList(fullAlbum.getTracks().getItems()));
        Map<String, Object> map = mapGeneric(fullAlbum, Album.class);
        map.put("artistsIds", refinedArtists);
        map.remove("artists");
        map.put("trackIds", tracksIds);
        map.remove("tracks");
        map.remove("albumType");
        map.remove("availableMarkets");
        map.remove("copyrights");
        map.remove("genres");
        map.remove("images");
        map.remove("label");
        map.remove("releaseDatePrecision");
        String[] dateAsString = map.get("releaseDate").toString().split("-");
        LocalDate localDate = LocalDate.of(Integer.parseInt(dateAsString[0]), Integer.parseInt(dateAsString[1]), Integer.parseInt(dateAsString[2]));
        map.put("releaseDate", new Date(localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()));
        return (RefinedAlbum) RefinedObjectBuilder.start(RefinedAlbum.class).with(map).build();
    }

    public static RefinedTrack mapTrack(Track fullTrack) throws MalformedURLException {
        Map<String, Object> map = mapGeneric(fullTrack, Track.class);
        map.remove("availableMarkets");
        map.forEach(DownstreamMapper::getArtistIds);
        map.put("artistIds", listOfIds);
        listOfIds = new ArrayList<>();
        map.remove("artists");
        map.forEach(DownstreamMapper::getAlbumIds);
        map.put("albumId", listOfIds.get(0));
        map.remove("album");
        return (RefinedTrack) RefinedObjectBuilder.start(RefinedTrack.class).with(map).build();
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

    public static RefinedUser mapUser(User fullUser) throws MalformedURLException {
        return (RefinedUser) RefinedObjectBuilder.start(RefinedUser.class).with(mapGeneric(fullUser, User.class)).build();

    }


    public static RefinedPlaylist mapPlaylist(Playlist fullPlaylist) throws MalformedURLException {
        return (RefinedPlaylist) RefinedObjectBuilder.start(RefinedPlaylist.class).with(mapGeneric(fullPlaylist, Playlist.class)).build();
    }

    public static <T> Map<String, Object> mapGeneric(T instance, Class<T> clazz) throws MalformedURLException {
        Map<String, Object> instanceMap = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Method getter;
                if (field.getName().equals("explicit")) {
                    getter = clazz.getDeclaredMethod("getIs" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1));
                } else {
                    getter = clazz.getDeclaredMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1));
                }
                if (invokeSimpleGetter(getter, instance) != null)
                    instanceMap.put(field.getName(), invokeSimpleGetter(getter, instance));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        instanceMap.remove("type");
        instanceMap.put("externalURL", new URL(((ExternalUrl) instanceMap.get("externalUrls")).getExternalUrls().get("spotify")));
        instanceMap.remove("externalUrls");
        instanceMap.put("spotifyURI", instanceMap.get("uri"));
        instanceMap.remove("uri");
        instanceMap.remove("externalIds");
        return instanceMap;
    }

    private static void getArtistIds(String key, Object value) {
        if (key.equals("artists")) {
            for (ArtistSimplified artistSimplified : (ArtistSimplified[]) value) {
                listOfIds.add(artistSimplified.getId());
            }
        }
    }

    private static void getAlbumIds(String key, Object value) {
        if (key.equals("album")) {
            if (value != null) {
                AlbumSimplified albumSimplified = (AlbumSimplified) value;
                listOfIds.add(albumSimplified.getId());
            }
        }
    }
}
