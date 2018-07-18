package al.miller.sv.svas.controller;

import al.miller.sv.ds.util.SortOrder;
import al.miller.sv.svas.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedAlbum;

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
    public RefinedAlbum getById(String albumId) {
        return albumService.getById(albumId);
    }

    @RequestMapping(path = "/most-popular", method = GET)
    @Override
    public List<RefinedAlbum> getMostPopularDefault() {
        return albumService.getMostPopular(0, 25);
    }

    @RequestMapping(path = "/most-popular?offset={offset}", method = GET)
    @Override
    public List<RefinedAlbum> getMostPopularOffsetOnly(@PathVariable("offset") int offset) {
        inputValidationOffsetOnly(offset);
        return albumService.getMostPopular(offset + 25, offset);
    }

    @RequestMapping(path = "/most-popular?limit={limit}", method = GET)
    @Override
    public List<RefinedAlbum> getMostPopularLimitOnly(@PathVariable("limit") int limit) {
        inputValidationLimitOnly(limit);
        return albumService.getMostPopular(limit, 0);
    }

    @RequestMapping(path = "/most-popular?offset={offset}&limit={limit}", method = GET)
    @Override
    public List<RefinedAlbum> getMostPopular(@PathVariable("limit") int limit, @PathVariable("offset") int offset) {
        inputValidation(limit, offset);
        return albumService.getMostPopular(limit, offset);
    }

    @RequestMapping(path = "/{id}/artist-id", method = GET)
    public List<RefinedAlbum> getAllFromArtistIdDefault(@PathVariable("id") String artistId) {
        return albumService.getAllFromArtistId(0, 25, artistId);
    }

    @RequestMapping(path = "/{id}/artist-id?offset={offset}", method = GET)
    public List<RefinedAlbum> getAllFromArtistIdOnlyOffset(@PathVariable("id") String artistId, @PathVariable("offset") int offset) {
        inputValidationOffsetOnly(offset);
        return albumService.getAllFromArtistId(offset, offset + 25, artistId);
    }

    @RequestMapping(path = "/{id}/artist-id?limit={limit}", method = GET)
    public List<RefinedAlbum> getAllFromArtistIdOnlyLimit(@PathVariable("id") String artistId, @PathVariable("limit") int limit) {
        inputValidationLimitOnly(limit);
        return albumService.getAllFromArtistId(0, limit, artistId);
    }

    @RequestMapping(path = "/{id}/artist-id?offset={offset}&limit={limit}", method = GET)
    public List<RefinedAlbum> getAllFromArtistId(@PathVariable("id") String artistId, @PathVariable("limit") int limit, @PathVariable("offset") int offset) {
        inputValidation(limit, offset);
        return albumService.getAllFromArtistId(offset, limit, artistId);
    }

    @RequestMapping(path = "/{id}/track-id", method = GET)
    public List<RefinedAlbum> getAllFromTrackIdDefault(@PathVariable("id") String trackId) {
        return albumService.getAllContainingTrack(0, 25, trackId);
    }

    @RequestMapping(path = "/{id}/track-id?offset={offset}", method = GET)
    public List<RefinedAlbum> getAllFromTrackIdOnlyOffset(@PathVariable("id") String trackId, @PathVariable("offset") int offset) {
        inputValidationOffsetOnly(offset);
        return albumService.getAllContainingTrack(offset, offset + 25, trackId);
    }

    @RequestMapping(path = "/{id}/track-id?limit={limit}", method = GET)
    public List<RefinedAlbum> getAllFromTrackIdOnlyLimit(@PathVariable("id") String trackId, @PathVariable("limit") int limit) {
        inputValidationLimitOnly(limit);
        return albumService.getAllContainingTrack(0, limit, trackId);
    }

    @RequestMapping(path = "/{id}/track-id?offset={offset}&limit={limit}", method = GET)
    public List<RefinedAlbum> getAllFromTrackId(@PathVariable("id") String trackId, @PathVariable("limit") int limit, @PathVariable("offset") int offset) {
        inputValidation(limit, offset);
        return albumService.getAllContainingTrack(offset, limit, trackId);
    }


    @RequestMapping(path = "/{sortOrder}", method = GET)
    public List<RefinedAlbum> getByReleaseDateDefault(@PathVariable("sortOrder") SortOrder sortOrder) {
        return albumService.getByReleaseDate(0, 25, sortOrder);
    }

    @RequestMapping(path = "/{sortOrder}?offset={offset}", method = GET)
    public List<RefinedAlbum> getByReleaseDateOnlyOffset(@PathVariable("sortOrder") SortOrder sortOrder, @PathVariable("offset") int offset) {
        inputValidationOffsetOnly(offset);
        return albumService.getByReleaseDate(offset, offset + 25, sortOrder);
    }

    @RequestMapping(path = "/{sortOrder}?limit={limit}", method = GET)
    public List<RefinedAlbum> getByReleaseDateOnlyLimit(@PathVariable("sortOrder") SortOrder sortOrder, @PathVariable("limit") int limit) {
        inputValidationLimitOnly(limit);
        return albumService.getByReleaseDate(0, limit, sortOrder);
    }

    @RequestMapping(path = "/{sortOrder}?offset={offset}&limit={limit}", method = GET)
    public List<RefinedAlbum> getByReleaseDate(@PathVariable("sortOrder") SortOrder sortOrder, @PathVariable("limit") int limit, @PathVariable("offset") int offset) {
        inputValidation(limit, offset);
        return albumService.getByReleaseDate(offset, limit, sortOrder);
    }


    @RequestMapping(path = "/date/{date}", method = GET)
    public List<RefinedAlbum> getAlbumsFromDateDefault(@PathVariable("date") long dateFromEpoch) {
        return albumService.getAlbumsFromDate(0, 25, new Date(dateFromEpoch));
    }

    @RequestMapping(path = "/date/{date}?offset={offset}", method = GET)
    public List<RefinedAlbum> getAlbumsFromDateOnlyOffset(@PathVariable("date") long dateFromEpoch, @PathVariable("offset") int offset) {
        inputValidationOffsetOnly(offset);
        return albumService.getAlbumsFromDate(offset, offset + 25, new Date(dateFromEpoch));
    }

    @RequestMapping(path = "/date/{date}?limit={limit}", method = GET)
    public List<RefinedAlbum> getAlbumsFromDateOnlyLimit(@PathVariable("date") long dateFromEpoch, @PathVariable("limit") int limit) {
        inputValidationLimitOnly(limit);
        return albumService.getAlbumsFromDate(0, limit, new Date(dateFromEpoch));
    }

    @RequestMapping(path = "/date/{date}?offset={offset}&limit={limit}", method = GET)
    public List<RefinedAlbum> getAlbumsFromDate(@PathVariable("date") long dateFromEpoch, @PathVariable("limit") int limit, @PathVariable("offset") int offset) {
        inputValidation(limit, offset);
        return albumService.getAlbumsFromDate(offset, limit, new Date(dateFromEpoch));
    }


    @Override
    protected AlbumService getService() {
        return albumService;
    }

}
