package al.miller.sv.svas.controller;

import al.miller.sv.svas.service.ArtistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedArtist;

import java.util.Arrays;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("api/v1/artists")
public class ArtistController extends AbstractController<RefinedArtist> {

    private ArtistService artistService;

    @Autowired
    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @RequestMapping(path = "/{id}", method = GET)
    @Override
    public RefinedArtist getById(@PathVariable("id") String artistId) {
        return artistService.getById(artistId);
    }

    @RequestMapping(path = "/{ids}/multi", method = GET)
    @Override
    public List<RefinedArtist> getMultipleById(@PathVariable("ids") String ids) {
        List<String> idList = Arrays.asList(ids.split("&"));
        return artistService.getMultipleById(idList);
    }

    @RequestMapping(path = "/most-popular", method = GET)
    @Override
    public List<RefinedArtist> getMostPopular(@RequestParam("limit") int limit, @RequestParam("offset") int offset) {
        inputValidation(offset, limit);
        return artistService.getMostPopular(offset,limit);
    }

    @RequestMapping(path = "/genre/{genre}", method = GET)
    public List<RefinedArtist> getAllArtistsForGenre(@PathVariable("genre") String genre, @RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        inputValidation(offset, limit);
        return artistService.getForGenre(offset, limit, genre);
    }

    @RequestMapping(path = "/most-followed", method = GET)
    public List<RefinedArtist> getMostFollowedArtists(@RequestParam("offset") int offset, @RequestParam("limit") int limit) throws IllegalArgumentException {
        inputValidation(offset, limit);
        return artistService.getMostFollowed(offset, limit);
    }

    @RequestMapping(path = "/on-album/{id}", method = GET)
    public List<RefinedArtist> getAlbumsArtists(@PathVariable("id") String albumId) {
        return artistService.getArtistsOnAlbum(albumId);
    }

    @Override
    protected ArtistService getService() {
        return artistService;
    }
}
