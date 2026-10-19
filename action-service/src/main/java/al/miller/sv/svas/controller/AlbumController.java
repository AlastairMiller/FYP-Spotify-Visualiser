package al.miller.sv.svas.controller;

import al.miller.sv.ds.util.SortOrder;
import al.miller.sv.svas.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedAlbum;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("api/v1/albums")
public class AlbumController extends AbstractController<RefinedAlbum> {

    private AlbumService albumService;

    @Autowired
    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @RequestMapping(path = "/{id}", method = GET)
    @Override
    public RefinedAlbum getById(@PathVariable("id") String albumId) {
        return albumService.getById(albumId);
    }

    @RequestMapping(path = "/{ids}/multi", method = GET)
    public List<RefinedAlbum> getMultipleById(@PathVariable("ids") String ids) {
        List<String> idList = Arrays.asList(ids.split("&"));
        return albumService.getMultipleById(idList);
    }

    @RequestMapping(path = "/name={searchTerm}", method = GET)
    @Override
    public List<RefinedAlbum> search(@PathVariable("searchTerm") String searchTerm, @RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        return albumService.search(searchTerm, offset, limit);
    }

    @RequestMapping(path = "/most-popular", method = GET)
    public List<RefinedAlbum> getMostPopular(@RequestParam("offset") int offset,
                                             @RequestParam("limit") int limit) {
        inputValidation(offset, limit);
        return albumService.getMostPopular(offset, limit);
    }

    @RequestMapping(path = "/from-artist-id/{id}", method = GET)
    public List<RefinedAlbum> getAllFromArtistId(@PathVariable("id") String artistId, @RequestParam(value = "offset") int offset, @RequestParam(value = "limit") int limit) {
        inputValidation(offset, limit);
        return albumService.getAllFromArtistId(offset, limit, artistId);
    }

    @RequestMapping(path = "from-track-id/{id}", method = GET)
    public List<RefinedAlbum> getAllFromTrackId(@PathVariable("id") String trackId, @RequestParam("limit") int limit, @RequestParam("offset") int offset) {
        inputValidation(offset, limit);
        return albumService.getAllContainingTrack(offset, limit, trackId);
    }

    @RequestMapping(path = "/release-date/{sortOrder}", method = GET)
    public List<RefinedAlbum> getByReleaseDate(@PathVariable("sortOrder") SortOrder sortOrder, @RequestParam("limit") int limit, @RequestParam("offset") int offset) {
        inputValidation(offset, limit);
        return albumService.getByReleaseDate(offset, limit, sortOrder);
    }

    @RequestMapping(path = "/date/{date}", method = GET)
    public List<RefinedAlbum> getAlbumsFromDate(@PathVariable("date") long dateFromEpoch, @RequestParam("limit") int limit, @RequestParam("offset") int offset) {
        inputValidation(offset, limit);
        return albumService.getAlbumsFromDate(offset, limit, new Date(dateFromEpoch));
    }


    @Override
    protected AlbumService getService() {
        return albumService;
    }

}
