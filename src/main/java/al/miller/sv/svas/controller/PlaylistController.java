package al.miller.sv.svas.controller;

import al.miller.sv.svas.service.AbstractService;
import al.miller.sv.svas.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedPlaylist;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("api/v1/playlists")
public class PlaylistController extends AbstractController<RefinedPlaylist> {

    private PlaylistService playlistService;

    @Autowired
    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @RequestMapping(path="/withtrack/{id}", method = GET)
    public List<RefinedPlaylist> getAllPlaylistsContainingSong(@PathVariable("id") String id) {
        return playlistService.getAllPlaylistsContainingSong(id);
    }

    @RequestMapping(path = "/withcountrycode/{id}", method = GET)
    public List<RefinedPlaylist> getAllPlaylistsInCountry(@PathVariable("id") String countryCode) throws IllegalArgumentException {
        validateCountryCode(countryCode);
        return playlistService.getPlaylistsForCountry(countryCode);
    }

    @Override
    protected AbstractService<RefinedPlaylist> getService() {
        return playlistService;
    }

    private void validateCountryCode(String countryCode){

    }
}
