package al.miller.sv.svas.controller;

import al.miller.sv.svas.service.ArtistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedArtist;

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

    @RequestMapping(path = "/genre/{genre}", method = GET)
    public List<RefinedArtist> getAllArtistsForGenreDefault(@PathVariable("genre") String genre) {
        return artistService.getForGenre(0, 50, genre);
    }

    @RequestMapping(path = "/genre/{genre}?offset={offset}", method = GET)
    public List<RefinedArtist> getAllArtistsForGenreOffset(@PathVariable("genre") String genre, @PathVariable("offset") int offset) {
        inputValidationOffsetOnly(offset);
        return artistService.getForGenre(offset, 50, genre);
    }

    @RequestMapping(path = "/genre/{genre}?limit={limit}", method = GET)
    public List<RefinedArtist> getAllArtistsForGenreLimit(@PathVariable("genre") String genre, @PathVariable("limit") int limit) {
        inputValidationLimitOnly(limit);
        return artistService.getForGenre(0, limit, genre);
    }

    @RequestMapping(path = "/genre/{genre}?offset={offset}&limit={limit}", method = GET)
    public List<RefinedArtist> getAllArtistsForGenre(@PathVariable("genre") String genre, @PathVariable("offset") int offset, @PathVariable("limit") int limit) {
        inputValidation(offset, limit);
        return artistService.getForGenre(offset, limit, genre);
    }

    @RequestMapping(path = "/mostfollowed", method = GET)
    public List<RefinedArtist> getMostFollowedArtistsDefault() {
        return getMostFollowedArtists(0, 50);
    }

    @RequestMapping(path = "/mostfollowed?offset={offset}", method = GET)
    public List<RefinedArtist> getMostFollowedArtistsOffsetOnly(@PathVariable("offset") int offset) {
        inputValidationLimitOnly(offset);
        return getMostFollowedArtists(offset, offset + 50);
    }

    @RequestMapping(path = "/mostfollowed?limit={limit}", method = GET)
    public List<RefinedArtist> getMostFollowedArtistsLimitOnly(@PathVariable("limit") int limit) {
        inputValidationLimitOnly(limit);
        return getMostFollowedArtists(0, limit);
    }

    @RequestMapping(path = "/mostfollowed?offset={offset}&limit={limit}", method = GET)
    public List<RefinedArtist> getMostFollowedArtists(@PathVariable("offset") int offset, @PathVariable("limit") int limit) throws IllegalArgumentException {
        inputValidation(offset, limit);
        return artistService.getMostFollowed(offset, limit);
    }

    @Override
    protected ArtistService getService() {
        return artistService;
    }
}
