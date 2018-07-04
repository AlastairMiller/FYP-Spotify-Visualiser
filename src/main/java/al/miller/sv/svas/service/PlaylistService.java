package al.miller.sv.svas.service;

import al.miller.sv.ds.dao.RefinedPlaylistDao;
import org.springframework.stereotype.Service;
import refinedDataModels.RefinedPlaylist;

import java.util.List;

@Service
public class PlaylistService extends AbstractService<RefinedPlaylist> {

    public PlaylistService(RefinedPlaylistDao dao) {
        super(dao);
    }

    public List<RefinedPlaylist> getAllPlaylistsContainingSong(String id) {
    }

    public List<RefinedPlaylist> getPlaylistsForCountry(String countryCode) {
    }
}
