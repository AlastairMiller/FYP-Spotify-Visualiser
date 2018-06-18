package com.sv.svas.controller;

import com.sv.svas.service.AbstractService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedPlaylist;

import java.util.List;

@RestController
@RequestMapping("api/v1/playlists")
public class PlaylistController extends AbstractController<RefinedPlaylist> {

    public PlaylistController(AbstractService<RefinedPlaylist> playlistService) {
        super(playlistService);
    }

    public List<RefinedPlaylist> getAllPlaylistsContainingSong(String id) {
    }

    public List<RefinedPlaylist> getAllPlaylistsInCountry(String countryCode) {
    }

}
