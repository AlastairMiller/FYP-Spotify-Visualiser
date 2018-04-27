package com.sv.svas.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedPlaylist;

import java.util.List;

@RestController
@RequestMapping("api/v1/playlists")
public class PlaylistController {

    public List<RefinedPlaylist> getAllPlaylistsContainingSong(String id){}

    public List<RefinedPlaylist> getAllPlaylistsInCountry(String countryCode){}

    public RefinedPlaylist getPlaylistById(String id){}


}
