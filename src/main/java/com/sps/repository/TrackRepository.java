package com.sps.repository;

import com.sps.model.RefinedTrack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<RefinedTrack, String> {
    RefinedTrack findById(String id);
}
