package al.miller.sv.svas.controller;

import al.miller.sv.svas.service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedPlaylist;
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


    @RequestMapping(path = "/{id}", method = GET)
    @Override
    public RefinedTrack getById(String albumId) {
        return trackService.getById(albumId);
    }

    @RequestMapping(path = "/most-popular", method = GET)
    @Override
    public List<RefinedTrack> getMostPopularDefault() {
        return trackService.getMostPopular(0, 25);
    }

    @RequestMapping(path = "/most-popular?offset={offset}", method = GET)
    @Override
    public List<RefinedTrack> getMostPopularOffsetOnly(@PathVariable("offset") int offset) {
        inputValidationOffsetOnly(offset);
        return trackService.getMostPopular(offset + 25, offset);
    }

    @RequestMapping(path = "/most-popular?limit={limit}", method = GET)
    @Override
    public List<RefinedTrack> getMostPopularLimitOnly(@PathVariable("limit") int limit) {
        inputValidationLimitOnly(limit);
        return trackService.getMostPopular(limit, 0);
    }

    @RequestMapping(path = "/most-popular?offset={offset}&limit={limit}", method = GET)
    @Override
    public List<RefinedTrack> getMostPopular(@PathVariable("limit") int limit, @PathVariable("offset") int offset) {
        inputValidation(limit, offset);
        return trackService.getMostPopular(limit, offset);
    }


    @RequestMapping(path = "/for-playlist/{id}", method = GET)
    public List<RefinedTrack> getMostPopularTracksFromPlaylistDefault(@PathVariable("id") String playlistId) {
        return getMostPopularTracksFromPlaylist(playlistId, 0, 0);
    }

    @RequestMapping(path = "/for-playlist/{id}?offset={offset}", method = GET)
    public List<RefinedTrack> getMostTracksFromPlaysetOffsetOnly(@PathVariable("id") String playlistId, @PathVariable("offset") int offset) {
        inputValidationOffsetOnly(offset);
        return getMostPopularTracksFromPlaylist(playlistId, offset, offset + 25);
    }

    @RequestMapping(path = "/for-playlist/{id}?limit={limit}", method = GET)
    public List<RefinedTrack> getMostTracksFromPlaysetLimitOnly(@PathVariable("id") String playlistId, @PathVariable("limit") int limit) {
        inputValidationLimitOnly(limit);
        return getMostPopularTracksFromPlaylist(playlistId, 0, limit);
    }

    @RequestMapping(path = "/for-playlist/{id}?offset={offset}&limit={limit}", method = GET)
    public List<RefinedTrack> getMostPopularTracksFromPlaylist(@PathVariable("id") String playlistId, @PathVariable("offset") int offset, @PathVariable("limit") int limit) throws IllegalArgumentException {
        inputValidation(offset, limit);
        return trackService.getMostPopularFromPlaylist(offset, limit, playlistId);
    }

    @RequestMapping(path = "/for-artist/{id}", method = GET)
    public List<RefinedTrack> getAllTracksForArtist(@PathVariable("id") String artistId) {
        return trackService.getAllByArtist(artistId);
    }

    @RequestMapping(path = "/on-album/{id}", method = GET)
    public List<RefinedTrack> getAlbumsTracks(@PathVariable("id") String albumId) {
        return trackService.getAllByAlbum(albumId);
    }

    @Override
    protected TrackService getService() {
        return trackService;
    }
}


