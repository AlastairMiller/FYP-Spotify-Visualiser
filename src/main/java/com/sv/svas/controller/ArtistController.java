package com.sv.svas.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import refinedDataModels.RefinedArtist;
import refinedDataModels.RefinedTrack;

import java.util.List;

@RestController
@RequestMapping("api/v1/artists")
public class ArtistController {

    public List<RefinedArtist> getAllArtistsForGenre(String genre){}

    public List<RefinedArtist> getMostPopularArtists(int offset, int limit){}

    public List<RefinedArtist> getMostFollowedArtists(int offset, int limit){}



}
