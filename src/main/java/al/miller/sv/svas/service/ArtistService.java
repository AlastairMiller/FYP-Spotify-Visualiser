package al.miller.sv.svas.service;

import al.miller.sv.ds.dao.RefinedArtistDao;
import al.miller.sv.svas.MongoHelper;
import org.springframework.stereotype.Service;
import refinedDataModels.RefinedArtist;

import java.util.List;

@Service
public class ArtistService extends AbstractService<RefinedArtistDao> {

    public ArtistService(MongoHelper mongoHelper) {
        super(mongoHelper);
        this.dao = mongoHelper.getRefinedArtistDao();
    }

    public List<RefinedArtist> getForGenre(int offset, int limit, String genre) {
        return dao.getAllByGenre(offset, limit, genre);
    }

    public List<RefinedArtist> getMostFollowed(int offset, int limit) {
        return dao.getMostFollowedArtists(offset, limit);
    }
}
