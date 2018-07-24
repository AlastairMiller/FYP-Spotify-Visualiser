package al.miller.sv.svas.controller;

import al.miller.sv.svas.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedPlaylist;

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
    public RefinedPlaylist getById(@PathVariable("id") String albumId) {
        return playlistService.getById(albumId);
    }

    @RequestMapping(path = "/most-popular", method = GET)
    @Override
    public List<RefinedPlaylist> getMostPopularDefault() {
        return playlistService.getMostPopular(0, 25);
    }

    @RequestMapping(path = "/most-popular?offset={offset}", method = GET)
    @Override
    public List<RefinedPlaylist> getMostPopularOffsetOnly(@PathVariable("offset") int offset) {
        inputValidationOffsetOnly(offset);
        return playlistService.getMostPopular(offset + 25, offset);
    }

    @RequestMapping(path = "/most-popular?limit={limit}", method = GET)
    @Override
    public List<RefinedPlaylist> getMostPopularLimitOnly(@PathVariable("limit") int limit) {
        inputValidationLimitOnly(limit);
        return playlistService.getMostPopular(limit, 0);
    }

    @RequestMapping(path = "/most-popular?offset={offset}&limit={limit}", method = GET)
    @Override
    public List<RefinedPlaylist> getMostPopular(@PathVariable("limit") int limit, @PathVariable("offset") int offset) {
        inputValidation(limit, offset);
        return playlistService.getMostPopular(limit, offset);
    }

    @RequestMapping(path = "/by-user-id/{id}", method = GET)
    public List<RefinedPlaylist> getByUserIdDefault(@PathVariable("id") String userId) {
        return playlistService.getByUserId(userId, 0, 25);
    }


    @RequestMapping(path = "/by-user-id/{id}?offset={offset}", method = GET)
    public List<RefinedPlaylist> getByUserIdOffsetOnly(@PathVariable("id") String userId, @PathVariable("offset") int offset) {
        inputValidationOffsetOnly(offset);
        return playlistService.getByUserId(userId, offset, offset + 25);
    }

    @RequestMapping(path = "/by-user-id/{id}?limit={limit}", method = GET)
    public List<RefinedPlaylist> getByUserIdLimitOnly(@PathVariable("id") String userId, @PathVariable("limit") int limit) {
        inputValidationLimitOnly(limit);
        return playlistService.getByUserId(userId, 0, limit);
    }

    @RequestMapping(path = "/by-user-id/{id}?offset={offset}&limit={limit}", method = GET)
    public List<RefinedPlaylist> getByUserId(@PathVariable("id") String userId, @PathVariable("offset") int offset, @PathVariable("limit") int limit) {
        inputValidation(offset, limit);
        return playlistService.getByUserId(userId, offset, limit);
    }

    @RequestMapping(path = "/with-track/{id}", method = GET)
    public List<RefinedPlaylist> getAllPlaylistsContainingSongDefault(@PathVariable("id") String id) {
        return playlistService.getAllPlaylistsContainingSong(0, 25, id);
    }

    @RequestMapping(path = "/with-track/{id}?offset={offset}", method = GET)
    public List<RefinedPlaylist> getAllPlaylistsContainingSongOffsetOnly(@PathVariable("id") String id, @PathVariable("offset") int offset) {
        inputValidation(offset, offset + 25);
        return playlistService.getAllPlaylistsContainingSong(offset, offset + 25, id);
    }

    @RequestMapping(path = "/with-track/{id}?limit={limit}", method = GET)
    public List<RefinedPlaylist> getAllPlaylistsContainingSongLimitOnly(@PathVariable("id") String id, @PathVariable("limit") int limit) {
        inputValidation(0, limit);
        return playlistService.getAllPlaylistsContainingSong(0, limit, id);
    }

    @RequestMapping(path = "/with-track/{id}?offset={offset}&limit={limit}", method = GET)
    public List<RefinedPlaylist> getAllPlaylistsContainingSong(@PathVariable("id") String id, @PathVariable("offset") int offset, @PathVariable("limit") int limit) {
        inputValidation(offset, limit);
        return playlistService.getAllPlaylistsContainingSong(offset, limit, id);
    }

    @RequestMapping(path = "/with-country-code/{id}", method = GET)
    public List<RefinedPlaylist> getAllPlaylistsInCountryDefault(@PathVariable("id") String countryCode) throws IllegalArgumentException {
        if (validateCountryCode(countryCode)) {
            return playlistService.getPlaylistsForCountry(countryCode, 0, 25);
        } else {
            throw new IllegalArgumentException("Invalid Country Code Requested");
        }
    }

    @RequestMapping(path = "/with-country-code/{id}?offset={offset}", method = GET)
    public List<RefinedPlaylist> getAllPlaylistsInCountryOffsetOnly(@PathVariable("id") String countryCode, @PathVariable("offset") int offset) throws IllegalArgumentException {
        if (validateCountryCode(countryCode)) {
            return playlistService.getPlaylistsForCountry(countryCode, offset, 25);
        } else {
            throw new IllegalArgumentException("Invalid Country Code Requested");
        }
    }

    @RequestMapping(path = "/with-country-code/{id}?limit={limit}", method = GET)
    public List<RefinedPlaylist> getAllPlaylistsInCountryLimitOnly(@PathVariable("id") String countryCode, @PathVariable("limit") int limit) throws IllegalArgumentException {
        if (validateCountryCode(countryCode)) {
            return playlistService.getPlaylistsForCountry(countryCode, 0, limit);
        } else {
            throw new IllegalArgumentException("Invalid Country Code Requested");
        }
    }

    @RequestMapping(path = "/with-country-code/{id}?offset={offset}&limit={limit}", method = GET)
    public List<RefinedPlaylist> getAllPlaylistsInCountry(@PathVariable("id") String countryCode, @PathVariable("offset") int offset, @PathVariable("limit") int limit) throws IllegalArgumentException {
        if (validateCountryCode(countryCode)) {
            return playlistService.getPlaylistsForCountry(countryCode, offset, limit);
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
