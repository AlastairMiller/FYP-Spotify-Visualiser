package al.miller.sv.svas.service;

import al.miller.sv.ds.dao.RefinedAlbumDao;
import al.miller.sv.ds.dao.RefinedArtistDao;
import al.miller.sv.ds.dao.RefinedTrackDao;
import al.miller.sv.svas.MongoHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import refinedDataModels.RefinedArtist;
import refinedDataModels.RefinedTrack;

import java.util.List;

@Service
@Slf4j
public class AlbumService extends AbstractService<RefinedAlbumDao> {

    private RefinedArtistDao refinedArtistDao;
    private RefinedTrackDao refinedTrackDao;

    public AlbumService(MongoHelper mongoHelper){
        super(mongoHelper);
        refinedTrackDao = mongoHelper.getRefinedTrackDao();
        refinedArtistDao = mongoHelper.getRefinedArtistDao();
    }

    public List<RefinedArtist> getAlbumsArtists(String albumId) {
        List<String> artistIds = dao.getAlbumArtistIds(albumId);
        return refinedArtistDao.getMultipleById(artistIds);
    }

    public List<RefinedTrack> getAlbumsTracks(String albumId) {
        return refinedTrackDao.getAllOnAlbum(albumId);
    }
}
