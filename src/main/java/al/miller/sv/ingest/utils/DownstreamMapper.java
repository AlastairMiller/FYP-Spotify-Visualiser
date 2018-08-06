package al.miller.sv.ingest.utils;


import al.miller.sv.ds.util.RefinedObjectBuilder;
import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.enums.ReleaseDatePrecision;
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
import java.util.stream.Collectors;

import static al.miller.sv.ds.mapper.RefinedtoJsonMapper.invokeSimpleGetter;


@Slf4j
public class DownstreamMapper {

    private static <T> List<String> returnNestedIds(Class<?> clazz, List<T> objects) {
        List<String> nestedIds = new ArrayList<>();
        for (T object : objects) {
            if (clazz.getName().equals("com.wrapper.spotify.model_objects.specification.PlaylistTrack")) {
                try {
                    nestedIds.add(((PlaylistTrack) object).getTrack().getId());
                } catch (Exception e) {
                    log.error("Invalid playlist track: {}");
                }
            } else {
                try {
                    Field field = clazz.getDeclaredField("id");
                    Method getter = clazz.getDeclaredMethod("get" + field.getName().substring(0, 1).toUpperCase() +
                            field.getName().substring(1));
                    nestedIds.add(String.valueOf(getter.invoke(object)));
                } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return nestedIds;
    }


    public static RefinedAlbum mapAlbum(Album fullAlbum) throws MalformedURLException, InstantiationException, IllegalAccessException {
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
        LocalDate localDate = translateDate(map.get("releaseDate").toString(), (ReleaseDatePrecision) map.get("releaseDatePrecision"));
        map.put("releaseDate", new Date(localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()));
        map.remove("releaseDatePrecision");
        return (RefinedAlbum) RefinedObjectBuilder.start(RefinedAlbum.class).with(map).build();
    }

    private static LocalDate translateDate(String dateAsString, ReleaseDatePrecision precision) {
        String[] dateAsStringArray = dateAsString.split("-");
        switch (precision) {
            case DAY:
                return LocalDate.of(Integer.parseInt(dateAsStringArray[0]), Integer.parseInt(dateAsStringArray[1]), Integer.parseInt(dateAsStringArray[2]));
            case MONTH:
                return LocalDate.of(Integer.parseInt(dateAsStringArray[0]), Integer.parseInt(dateAsStringArray[1]), 1);
            case YEAR:
                return LocalDate.of(Integer.parseInt(dateAsStringArray[0]), 1, 1);
        }
        throw new RuntimeException();
    }

    public static RefinedTrack mapTrack(Track fullTrack) throws MalformedURLException, InstantiationException, IllegalAccessException {
        Map<String, Object> map = mapGeneric(fullTrack, Track.class);
        map.remove("availableMarkets");
        map.put("previewURL", map.get("previewUrl"));
        map.remove("previewUrl");
        map.put("artistIds", getArtistIds("artists", map.get("artists")));
        map.remove("artists");
        map.put("albumId", getAlbumIds("album", map.get("album")).get(0));
        map.remove("album");
        return (RefinedTrack) RefinedObjectBuilder.start(RefinedTrack.class).with(map).build();
    }

    public static RefinedArtist mapArtist(Artist fullArtist) throws MalformedURLException, InstantiationException, IllegalAccessException {
        Map<String, Object> map = mapGeneric(fullArtist, Artist.class);
        map.remove("images");
        map.replace("followers", ((Followers) map.get("followers")).getTotal());
        map.replace("genres", (Arrays.asList((String[]) map.get("genres"))));
        return (RefinedArtist) RefinedObjectBuilder.start(RefinedArtist.class).with(map).build();
    }

    public static RefinedUser mapUser(User fullUser) throws MalformedURLException, InstantiationException, IllegalAccessException {
        Map<String, Object> map = mapGeneric(fullUser, User.class);
        map.put("numOfFollowers", ((Followers) map.get("followers")).getTotal());
        map.put("imageURL", new URL(((Image[]) map.get("images"))[0].getUrl()));
        map.remove("followers");
        map.remove("images");
        map.remove("birthdate");
        map.remove("country");
        map.remove("email");
        map.remove("product");
        return (RefinedUser) RefinedObjectBuilder.start(RefinedUser.class).with(map).build();

    }


    public static RefinedPlaylist mapPlaylist(Playlist fullPlaylist) throws MalformedURLException, InstantiationException, IllegalAccessException {
        List<String> tracksIds = returnNestedIds(PlaylistTrack.class, Arrays.asList(fullPlaylist.getTracks().getItems()));
        Map<String, Object> map = mapGeneric(fullPlaylist, Playlist.class);
        CountryCode countryCode = CountryCode.getByCode(fullPlaylist.getName().substring(fullPlaylist.getName().length() - 2, fullPlaylist.getName().length()));
        map.remove("tracks");
        map.remove("collaborative");
        map.remove("description");
        map.remove("publicAccess");
        map.remove("snapshotId");
        map.put("userId", ((User) map.get("owner")).getId());
        map.remove("owner");
        map.put("imageURL", ((Image[]) map.get("images"))[0].getUrl());
        map.remove("images");
        map.put("numOfFollowers", ((Followers) map.get("followers")).getTotal());
        map.remove("followers");
        map.put("trackIds", tracksIds);
        map.put("countryCode", countryCode);
        return (RefinedPlaylist) RefinedObjectBuilder.start(RefinedPlaylist.class).with(map).build();
    }

    public static <T> Map<String, Object> mapGeneric(T instance, Class<T> clazz) throws MalformedURLException {
        Map<String, Object> instanceMap = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Method getter;
                //Dodgy Variable naming in Spotify api, some boolean getters called getIs`` while some boolean getters are named get`` :(
                if ((field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) && !field.getName().equals("isPlayable")) {
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

    private static List<String> getArtistIds(String key, Object value) {
        List<String> artistIds = new ArrayList<>();
        if (key.equals("artists")) {
            for (ArtistSimplified artistSimplified : (ArtistSimplified[]) value) {
                artistIds.add(artistSimplified.getId());
            }
        }
        return artistIds;
    }

    private static List<String> getAlbumIds(String key, Object value) {
        List<String> albumIds = new ArrayList<>();
        if (key.equals("album")) {
            if (value != null) {
                AlbumSimplified albumSimplified = (AlbumSimplified) value;
                albumIds.add(albumSimplified.getId());
            }
        }
        return albumIds;
    }
}
