package com.sv.svas.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedTrack;

import java.util.List;

@RestController
@RequestMapping("api/v1/tracks")
public class TrackController {

    public List<RefinedTrack> getMostPopularTracks(int limit, int offset){}

    public List<RefinedTrack> getMostPopularTracksFromPlaylist(int playlistId, int limit, int offset){}

    public RefinedTrack getTrackFromId(int id){}

    public List<RefinedTrack> getAllTracksForArtist(String artistId){}
}
