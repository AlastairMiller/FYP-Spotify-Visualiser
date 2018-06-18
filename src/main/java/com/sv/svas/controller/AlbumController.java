package com.sv.svas.controller;

import com.sv.svas.service.AbstractService;
import com.sv.svas.service.AlbumService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("api/v1/albums")
public class AlbumController extends AbstractController<RefinedAlbum> {

    private AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @Override
    protected AlbumService getService() {
        return albumService;
    }

    @RequestMapping(path = "/{id}/artists", method = GET)
    public List<RefinedArtist> getAlbumsArtists(@PathVariable("id") String albumId) {
        return albumService.getAlbumsArtists(albumId);
    }

    @RequestMapping(path = "/{id}/tracks", method = GET)
    public List<RefinedTrack> getAlbumsTracks(String albumId) {
        return null;
    }


}
