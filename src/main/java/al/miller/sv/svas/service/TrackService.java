package al.miller.sv.svas.service;

import al.miller.sv.ds.dao.RefinedTrackDao;
import al.miller.sv.svas.repository.AbstractDao;
import org.springframework.stereotype.Service;
import refinedDataModels.RefinedTrack;

import java.util.List;

@Service
public class TrackService extends AbstractService<RefinedTrack> {

    public TrackService(RefinedTrackDao trackDao) {
        super(trackDao);
    }

    public List<RefinedTrack> getPopularByPlaylist(int playlistId, int offset, int limit) {
    }

    public List<RefinedTrack> getAllByArtist(String artistId) {
    }
}
