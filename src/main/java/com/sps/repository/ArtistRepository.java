package com.sps.repository;

import com.sps.model.RefinedArtist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<RefinedArtist, String>{
    RefinedArtist findById(String id);
}
