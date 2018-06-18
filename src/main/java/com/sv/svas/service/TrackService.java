package com.sv.svas.service;

import com.sv.svas.repository.AbstractDao;
import org.springframework.stereotype.Service;
import refinedDataModels.RefinedTrack;

@Service
public class TrackService extends AbstractService<RefinedTrack> {
    public TrackService(AbstractDao<RefinedTrack> trackDao) {
        super(trackDao);
    }
}
