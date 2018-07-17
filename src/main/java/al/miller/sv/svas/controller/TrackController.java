package al.miller.sv.svas.controller;

import al.miller.sv.svas.service.AbstractService;
import al.miller.sv.svas.service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedTrack;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("api/v1/tracks")
public class TrackController extends AbstractController<RefinedTrack> {

    private TrackService trackService;

    @Autowired
    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @RequestMapping(path = "/forPlaylist/{id}", method = GET)
    public List<RefinedTrack> getMostPopularTracksFromPlaylistDefault(@PathVariable("id") int playlistId) {
        return getMostPopularTracksFromPlaylist(playlistId, 0, 0);
    }

    @RequestMapping(path = "/forPlaylist/{id}?offset={offset}", method = GET)
    public List<RefinedTrack> getMostTracksFromPlaysetOffsetOnly(@PathVariable("id") int playlistId, @PathVariable("offset") int offset) {
        return getMostPopularTracksFromPlaylist(playlistId, offset, offset + 50);
    }

    @RequestMapping(path = "/forPlaylist/{id}?limit={limit}", method = GET)
    public List<RefinedTrack> getMostTracksFromPlaysetLimitOnly(@PathVariable("id") int playlistId, @PathVariable("limit") int limit) {
        return getMostPopularTracksFromPlaylist(playlistId, 0, limit);
    }

    @RequestMapping(path = "/forPlaylist/{id}?offset={offset}&limit={limit}", method = GET)
    public List<RefinedTrack> getMostPopularTracksFromPlaylist(@PathVariable("id") int playlistId, @PathVariable("offset") int offset, @PathVariable("limit") int limit) throws IllegalArgumentException {
        inputValidation(offset, limit);
        return trackService.getMostPopularFromPlaylist(offset, limit, playlistId);
    }

    @RequestMapping(path = "/forartist/{id}", method = GET)
    public List<RefinedTrack> getAllTracksForArtist(@PathVariable("id") String artistId) {
        return trackService.getAllByArtist(artistId);
    }

    @Override
    protected TrackService getService() {
        return trackService;
    }
}


