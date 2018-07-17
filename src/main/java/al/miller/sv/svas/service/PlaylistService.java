package al.miller.sv.svas.service;

import al.miller.sv.ds.dao.RefinedPlaylistDao;
import al.miller.sv.svas.MongoHelper;
import org.springframework.stereotype.Service;
import refinedDataModels.RefinedPlaylist;

import java.util.List;

@Service
public class PlaylistService extends AbstractService<RefinedPlaylistDao> {

    public PlaylistService(MongoHelper mongoHelper) {
        super(mongoHelper);
        this.dao = mongoHelper.getRefinedPlaylistDao();
    }

    public List<RefinedPlaylist> getAllPlaylistsContainingSong(int offset, int limit, String id) {
        return dao.getPlaylistsContainingTrack(id, offset, limit);
    }

    //Todo implement this in datasource
    public List<RefinedPlaylist> getPlaylistsForCountry(String countryCode) {
        return dao.getAllMatchingNames(countryCode);
    }
}
