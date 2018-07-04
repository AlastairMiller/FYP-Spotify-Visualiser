package al.miller.sv.svas.service;

import al.miller.sv.ds.dao.RefinedAlbumDao;
import org.springframework.stereotype.Service;
import refinedDataModels.RefinedAlbum;
import refinedDataModels.RefinedArtist;
import refinedDataModels.RefinedTrack;

import java.util.List;

@Service
public class AlbumService extends AbstractService<RefinedAlbum> {
    public AlbumService(RefinedAlbumDao albumDao) {
        super(albumDao);
    }

    public List<RefinedArtist> getAlbumsArtists(String albumId) {
        return null;
    }

    public List<RefinedTrack> getAlbumsTracks(String albumId) {
    }
}
