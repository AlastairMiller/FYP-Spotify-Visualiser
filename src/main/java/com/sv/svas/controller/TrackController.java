package com.sv.svas.controller;

import com.sv.svas.service.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedTrack;

import java.util.List;

@RestController
@RequestMapping("api/v1/tracks")
public class TrackController extends AbstractController<RefinedTrack> {

    @Autowired
    public TrackController(AbstractService<RefinedTrack> trackService) {
        super(trackService);
    }

    public List<RefinedTrack> getMostPopularTracksFromPlaylist(int playlistId, int limit, int offset) {
        return null;
    }

    public List<RefinedTrack> getAllTracksForArtist(String artistId) {
        return null;
    }
}
