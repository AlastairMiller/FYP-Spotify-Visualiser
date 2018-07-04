package al.miller.sv.svas.service;

import al.miller.sv.ds.dao.RefinedArtistDao;
import org.springframework.stereotype.Service;
import refinedDataModels.RefinedArtist;

import java.util.List;

@Service
public class ArtistService extends AbstractService<RefinedArtist> {

    public ArtistService(RefinedArtistDao dao) {
        super(dao);
    }

    public List<RefinedArtist> getForGenre(String genre) {
    }

    public List<RefinedArtist> getMostFollowed(int offset, int limit) {
    }
}
