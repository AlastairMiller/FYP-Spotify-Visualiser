package al.miller.sv.svas.controller;

import al.miller.sv.svas.service.TrackService;
import com.neovisionaries.i18n.CountryCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedTrack;

import java.util.Arrays;
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
    public RefinedTrack getById(@PathVariable("id") String trackId) {
        return trackService.getById(trackId);
    }

    @RequestMapping(path = "/{ids}/multi", method = GET)
    @Override
    public List<RefinedTrack> getMultipleById(@PathVariable("ids") String ids) {
        List<String> idList = Arrays.asList(ids.split("&"));
        return trackService.getMultipleById(idList);
    }

    @RequestMapping(path = "/most-popular", method = GET)
    @Override
    public List<RefinedTrack> getMostPopular(@RequestParam("limit") int limit, @RequestParam("offset") int offset) {
        inputValidation(offset, limit);
        return trackService.getMostPopular(offset, limit);
    }

    @RequestMapping(path = "/for-playlist/{id}", method = GET)
    public List<RefinedTrack> getMostPopularTracksFromPlaylist(@PathVariable("id") String playlistId, @RequestParam("offset") int offset, @RequestParam("limit") int limit) throws IllegalArgumentException {
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

    @RequestMapping(path = "/random", method = GET)
    public RefinedTrack getRandom() {
        return trackService.getRandom();
    }

    @RequestMapping(path = "{id}/popular-country", method = GET)
    public CountryCode getPopularCountry(@PathVariable("id") String trackId) {
        return trackService.mostPopularIn(trackId);
    }

    @Override
    protected TrackService getService() {
        return trackService;
    }
}


