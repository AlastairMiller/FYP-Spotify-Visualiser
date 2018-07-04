package al.miller.sv.svas.controller;

import al.miller.sv.svas.service.AbstractService;
import al.miller.sv.svas.service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedTrack;

import java.util.List;

@RestController
@RequestMapping("api/v1/tracks")
public class TrackController extends AbstractController<RefinedTrack> {

    private TrackService trackService;

    @Autowired
    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    public List<RefinedTrack> getMostPopularTracksFromPlaylist(int playlistId, int offset, int limit) throws IllegalArgumentException {
        inputValidation(offset, limit);
        return trackService.getPopularByPlaylist(playlistId, offset, limit);
    }

    public List<RefinedTrack> getAllTracksForArtist(String artistId) {
        return trackService.getAllByArtist(artistId);
    }

    @Override
    protected AbstractService<RefinedTrack> getService() {
        return trackService;
    }
}


