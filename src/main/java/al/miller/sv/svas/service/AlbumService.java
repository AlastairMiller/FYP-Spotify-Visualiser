package al.miller.sv.svas.service;

import al.miller.sv.ds.dao.RefinedAlbumDao;
import al.miller.sv.ds.dao.RefinedArtistDao;
import al.miller.sv.ds.dao.RefinedTrackDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import refinedDataModels.RefinedAlbum;
import refinedDataModels.RefinedArtist;
import refinedDataModels.RefinedTrack;

import java.util.List;

@Service
public class AlbumService extends AbstractService<RefinedAlbum> {

    private RefinedArtistDao refinedArtistDao;
    private RefinedTrackDao refinedTrackDao;

    @Autowired
    public AlbumService(RefinedAlbumDao albumDao, RefinedArtistDao refinedArtistDao, RefinedTrackDao refinedTrackDao) {
        super(albumDao);
        this.refinedArtistDao = refinedArtistDao;
        this.refinedTrackDao = refinedTrackDao;
    }

    public List<RefinedArtist> getAlbumsArtists(String albumId) {
        List<String> artistIds = dao.getById(albumId).getArtistsIds();
        return refinedArtistDao.getMultipleById(artistIds);
    }

    public List<RefinedTrack> getAlbumsTracks(String albumId) {
        return refinedTrackDao.getAllOnAlbum(albumId);
    }
}
