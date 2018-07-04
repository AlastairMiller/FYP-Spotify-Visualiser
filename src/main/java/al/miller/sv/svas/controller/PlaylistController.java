package al.miller.sv.svas.controller;

import al.miller.sv.svas.service.AbstractService;
import al.miller.sv.svas.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedPlaylist;

import java.util.List;

@RestController
@RequestMapping("api/v1/playlists")
public class PlaylistController extends AbstractController<RefinedPlaylist> {

    private PlaylistService playlistService;

    @Autowired
    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    public List<RefinedPlaylist> getAllPlaylistsContainingSong(String id) {
        return playlistService.getAllPlaylistsContainingSong(id);
    }

    public List<RefinedPlaylist> getAllPlaylistsInCountry(String countryCode) throws IllegalArgumentException {
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
