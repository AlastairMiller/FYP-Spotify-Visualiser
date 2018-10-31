package al.miller.sv.svas.controller;

import al.miller.sv.svas.service.PlaylistService;
import com.neovisionaries.i18n.CountryCode;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedAlbum;
import refinedDataModels.RefinedPlaylist;

import javax.print.Doc;
import java.util.*;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("api/v1/playlists")
public class PlaylistController extends AbstractController<RefinedPlaylist> {

    private PlaylistService playlistService;
    private static final Set<String> ISO_COUNTRIES = new HashSet<>
            (Arrays.asList(Locale.getISOCountries()));


    @Autowired
    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }


    @RequestMapping(path = "/{id}", method = GET)
    @Override
    public RefinedPlaylist getById(@PathVariable("id") String playlistId) {
        return playlistService.getById(playlistId);
    }

    @RequestMapping(path = "/{ids}/multi", method = GET)
    @Override
    public List<RefinedPlaylist> getMultipleById(@PathVariable("ids") String ids) {
        List<String> idList = Arrays.asList(ids.split("&"));
        return playlistService.getMultipleById(idList);
    }

    @RequestMapping(path = "/most-popular", method = GET)
    @Override
    public List<RefinedPlaylist> getMostPopular(@RequestParam("limit") int limit, @RequestParam("offset") int offset) {
        inputValidation(offset, limit);
        return playlistService.getMostPopular(limit, offset);
    }

    @RequestMapping(path = "/by-user-id/{id}", method = GET)
    public List<RefinedPlaylist> getByUserId(@PathVariable("id") String userId, @RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        inputValidation(offset, limit);
        return playlistService.getByUserId(userId, offset, limit);
    }


    @RequestMapping(path = "/with-track/{id}", method = GET)
    public List<RefinedPlaylist> getAllPlaylistsContainingSong(@PathVariable("id") String id, @RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        inputValidation(offset, limit);
        return playlistService.getAllPlaylistsContainingSong(offset, limit, id);
    }

    @RequestMapping(path = "/country-code/{id}", method = GET)
    public List<RefinedPlaylist> getAllPlaylistsInCountry(@PathVariable("id") String countryCode, @RequestParam("offset") int offset, @RequestParam("limit") int limit) throws IllegalArgumentException {
        if (validateCountryCode(countryCode)) {
            return playlistService.getPlaylistsForCountry(countryCode, offset, limit);
        } else {
            throw new IllegalArgumentException("Invalid Country Code Requested");
        }
    }

    @RequestMapping(path = "/popular-tracks/{id}", method = GET)
    public List<Document> getPopularTracks(@PathVariable("id") String countryCode, @RequestParam("offset") int offset, @RequestParam("limit") int limit) throws IllegalArgumentException {
        if (validateCountryCode(countryCode)) {
            return playlistService.getMostPopularSongsForCountry(countryCode, offset, limit);
        } else {
            throw new IllegalArgumentException("Invalid Country Code Requested");
        }
    }

    @RequestMapping(path = "/popular-artists/{id}", method = GET)
    public List<Document> getPopularArtists(@PathVariable("id") String countryCode, @RequestParam("offset") int offset, @RequestParam("limit") int limit) throws IllegalArgumentException {
        if (validateCountryCode(countryCode)) {
            return playlistService.getMostPopularArtistsForCountry(countryCode, offset, limit);
        } else {
            throw new IllegalArgumentException("Invalid Country Code Requested");
        }
    }

    @RequestMapping(path = "/popular-genres/{id}", method = GET)
    public List<Document> getPopularGenres(@PathVariable("id") String countryCode, @RequestParam("offset") int offset, @RequestParam("limit") int limit) throws IllegalArgumentException {
        if (validateCountryCode(countryCode)) {
            return playlistService.getMostPopularGenresForCountry(countryCode, offset, limit);
        } else {
            throw new IllegalArgumentException("Invalid Country Code Requested");
        }
    }

    @Override
    protected PlaylistService getService() {
        return playlistService;
    }

    private boolean validateCountryCode(String countryCode) {
        return ISO_COUNTRIES.contains(countryCode);
    }
}
