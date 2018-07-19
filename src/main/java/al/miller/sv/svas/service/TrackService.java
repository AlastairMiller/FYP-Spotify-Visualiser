package al.miller.sv.svas.service;

import al.miller.sv.ds.dao.RefinedPlaylistDao;
import al.miller.sv.ds.dao.RefinedTrackDao;
import al.miller.sv.svas.MongoHelper;
import org.springframework.stereotype.Service;
import refinedDataModels.RefinedPlaylist;
import refinedDataModels.RefinedTrack;

import java.util.List;

@Service
public class TrackService extends AbstractService<RefinedTrackDao, RefinedTrack> {

    private RefinedPlaylistDao refinedPlaylistDao;

    public TrackService(MongoHelper mongoHelper) {
        super(mongoHelper);
        this.dao = mongoHelper.getRefinedTrackDao();
        this.refinedPlaylistDao = mongoHelper.getRefinedPlaylistDao();
    }

    public List<RefinedTrack> getAllByArtist(String artistId) {
        return dao.getAllFromArtist(artistId);
    }

    public List<RefinedTrack> getAllByAlbum(String albumId) {
        return dao.getAllOnAlbum(albumId);
    }

    public List<RefinedTrack> getMostPopularFromPlaylist(int offset, int limit, String playlistId) {
        RefinedPlaylist refinedPlaylist = refinedPlaylistDao.getById(playlistId);
        List<String> trackIds = refinedPlaylist.getTrackIds().subList(offset, limit);
        return dao.getMultipleById(trackIds);
    }
}
