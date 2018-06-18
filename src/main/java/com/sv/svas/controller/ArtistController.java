package com.sv.svas.controller;

import com.sv.svas.service.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedArtist;
import refinedDataModels.RefinedTrack;

import java.util.List;

@RestController
@RequestMapping("api/v1/artists")
public class ArtistController extends AbstractController<RefinedArtist> {

    @Autowired
    public ArtistController(AbstractService<RefinedArtist> service) {
        super(service);
    }

    public List<RefinedArtist> getAllArtistsForGenre(String genre){}

    public List<RefinedArtist> getMostFollowedArtists(int offset, int limit){}
}
