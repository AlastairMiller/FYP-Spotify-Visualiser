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

    @RequestMapping(path = "/withtrack/{id}", method = GET)
    public List<RefinedPlaylist> getAllPlaylistsContainingSongDefault(@PathVariable("id") String id) {
        return playlistService.getAllPlaylistsContainingSong(0, 25, id);
    }



    @RequestMapping(path = "/withcountrycode/{id}", method = GET)
    public List<RefinedPlaylist> getAllPlaylistsInCountry(@PathVariable("id") String countryCode) throws IllegalArgumentException {
        if (validateCountryCode(countryCode)) {
            return playlistService.getPlaylistsForCountry(countryCode);
        } else {
            throw new IllegalArgumentException("Invalid Country Code Requested");
        }
    }

    @Override
    protected PlaylistService getService() {
        return playlistService;
    }

    //TODO implement here?
    private boolean validateCountryCode(String countryCode) {
        return ISO_COUNTRIES.contains(countryCode);
    }
}
