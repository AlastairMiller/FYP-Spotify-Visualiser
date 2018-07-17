package al.miller.sv.svas.service;

import al.miller.sv.ds.dao.RefinedTrackDao;
import al.miller.sv.svas.MongoHelper;
import org.springframework.stereotype.Service;
import refinedDataModels.RefinedTrack;

import java.util.List;

@Service
public class TrackService extends AbstractService<RefinedTrackDao> {

    public TrackService(MongoHelper mongoHelper) {
        super(mongoHelper);
        this.dao = mongoHelper.getRefinedTrackDao();
    }

    public List<RefinedTrack> getAllByArtist(String artistId) {
        return dao.getAllFromArtist(artistId);
    }

    //TODO implement in dao
    public List<RefinedTrack> getMostPopularFromPlaylist(int offset, int limit, int playlistId) {
        return null;
    }
}
