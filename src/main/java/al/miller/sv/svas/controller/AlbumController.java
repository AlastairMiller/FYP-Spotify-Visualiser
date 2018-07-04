package al.miller.sv.svas.controller;

import al.miller.sv.svas.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedAlbum;
import refinedDataModels.RefinedArtist;
import refinedDataModels.RefinedTrack;


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

    @RequestMapping(path = "/{id}/artists", method = GET)
    public List<RefinedArtist> getAlbumsArtists(@PathVariable("id") String albumId) {
        return albumService.getAlbumsArtists(albumId);
    }

    @RequestMapping(path = "/{id}/tracks", method = GET)
    public List<RefinedTrack> getAlbumsTracks(String albumId) {
        return albumService.getAlbumsTracks(albumId) ;
    }

    @Override
    protected AlbumService getService() {
        return albumService;
    }

}
