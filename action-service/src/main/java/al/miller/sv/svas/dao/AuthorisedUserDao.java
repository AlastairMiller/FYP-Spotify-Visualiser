package al.miller.sv.svas.dao;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.data.browse.GetRecommendationsRequest;
import com.wrapper.spotify.requests.data.personalization.simplified.GetUsersTopArtistsRequest;
import com.wrapper.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;
import com.wrapper.spotify.requests.data.playlists.GetListOfUsersPlaylistsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class AuthorisedUserDao {

    public static List<PlaylistSimplified> pullUserPlaylists(SpotifyApi api, String userId) {
        final GetListOfUsersPlaylistsRequest request = api.getListOfUsersPlaylists(userId).build();
        try {
            Paging<PlaylistSimplified> playlistPaging = request.execute();
            return Arrays.asList(playlistPaging.getItems());
        } catch (IOException | SpotifyWebApiException e) {
            log.error(e.getMessage());
            return null;
        }

    }

    public static List<Artist> getTopArtists(SpotifyApi api) {
        final GetUsersTopArtistsRequest request = api.getUsersTopArtists().build();
        try {
            return Arrays.asList(request.execute().getItems());
        } catch (IOException | SpotifyWebApiException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static List<Track> getTopTracks(SpotifyApi api) {
        final GetUsersTopTracksRequest request = api.getUsersTopTracks().build();
        try {
            return Arrays.asList(request.execute().getItems());
        } catch (IOException | SpotifyWebApiException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static Recommendations recommedations(SpotifyApi api, List<Track> artists) {
        List<String> artistIds = artists.stream()
                .map(Track::getId)
                .collect(Collectors.toList());

        String artistIdsForRequest = artistIds.toString().replace("[", "").replace("]", "");
        final GetRecommendationsRequest recommendationsRequest = api.getRecommendations().seed_tracks(artistIds.get(0)).build();
        try {
            return recommendationsRequest.execute();
        } catch (IOException | SpotifyWebApiException e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
