package com.sv.svas.service;

import com.sv.svas.repository.AbstractDao;
import com.sv.svas.repository.AlbumDao;
import org.springframework.stereotype.Service;
import refinedDataModels.RefinedAlbum;
import refinedDataModels.RefinedArtist;

import java.util.List;

@Service
public class AlbumService extends AbstractService<RefinedAlbum> {
    public AlbumService(AlbumDao albumDao) {
        super(albumDao);
    }

    public List<RefinedArtist> getAlbumsArtists(String albumId) {
    }
}
