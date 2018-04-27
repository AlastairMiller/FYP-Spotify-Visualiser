package com.sv.svas.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedAlbum;
import refinedDataModels.RefinedArtist;
import refinedDataModels.RefinedTrack;

import java.util.List;

@RestController
@RequestMapping("api/v1/albums")
public class AlbumController {

    public List<RefinedArtist> getAlbumsArtists(String AlbumId){ }

    public List<RefinedTrack> getAlbumsTracks(String AlbumId){ }

    public List<RefinedAlbum> getAlbumById(String AlbumId)


}
