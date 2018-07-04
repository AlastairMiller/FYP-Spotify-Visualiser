package al.miller.sv.svas.controller;

import al.miller.sv.svas.service.AbstractService;
import al.miller.sv.svas.service.AlbumService;
import al.miller.sv.svas.service.ArtistService;
import com.wrapper.spotify.model_objects.specification.Artist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedArtist;
import refinedDataModels.RefinedTrack;

import java.util.List;

@RestController
@RequestMapping("api/v1/artists")
public class ArtistController extends AbstractController<RefinedArtist> {

    private ArtistService artistService;

    @Autowired
    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    public List<RefinedArtist> getAllArtistsForGenre(String genre) {
        return artistService.getForGenre(genre);
    }

    public List<RefinedArtist> getMostFollowedArtists(int offset, int limit) throws IllegalArgumentException {
        inputValidation(offset, limit);
        return artistService.getMostFollowed(offset,limit);
    }

    @Override
    protected AbstractService<RefinedArtist> getService() {
        return artistService;
    }
}
