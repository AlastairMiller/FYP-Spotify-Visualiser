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

    @RequestMapping(path = "/{id}", method = GET)
    @Override
    public RefinedArtist getById(@PathVariable("id") String albumId) {
        return artistService.getById(albumId);
    }

    @RequestMapping(path = "/most-popular", method = GET)
    @Override
    public List<RefinedArtist> getMostPopularDefault() {
        return artistService.getMostPopular(0, 25);
    }

    @RequestMapping(path = "/most-popular?offset={offset}", method = GET)
    @Override
    public List<RefinedArtist> getMostPopularOffsetOnly(@PathVariable("offset") int offset) {
        inputValidationOffsetOnly(offset);
        return artistService.getMostPopular(offset + 25, offset);
    }

    @RequestMapping(path = "/most-popular?limit={limit}", method = GET)
    @Override
    public List<RefinedArtist> getMostPopularLimitOnly(@PathVariable("limit") int limit) {
        inputValidationLimitOnly(limit);
        return artistService.getMostPopular(limit, 0);
    }

    @RequestMapping(path = "/most-popular?offset={offset}&limit={limit}", method = GET)
    @Override
    public List<RefinedArtist> getMostPopular(@PathVariable("limit") int limit, @PathVariable("offset") int offset) {
        inputValidation(limit, offset);
        return artistService.getMostPopular(limit, offset);
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

    @RequestMapping(path = "/most-followed", method = GET)
    public List<RefinedArtist> getMostFollowedArtistsDefault() {
        return getMostFollowedArtists(0, 50);
    }

    @RequestMapping(path = "/most-followed?offset={offset}", method = GET)
    public List<RefinedArtist> getMostFollowedArtistsOffsetOnly(@PathVariable("offset") int offset) {
        inputValidationLimitOnly(offset);
        return getMostFollowedArtists(offset, offset + 50);
    }

    @RequestMapping(path = "/most-followed?limit={limit}", method = GET)
    public List<RefinedArtist> getMostFollowedArtistsLimitOnly(@PathVariable("limit") int limit) {
        inputValidationLimitOnly(limit);
        return getMostFollowedArtists(0, limit);
    }

    @RequestMapping(path = "/most-followed?offset={offset}&limit={limit}", method = GET)
    public List<RefinedArtist> getMostFollowedArtists(@PathVariable("offset") int offset, @PathVariable("limit") int limit) throws IllegalArgumentException {
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
