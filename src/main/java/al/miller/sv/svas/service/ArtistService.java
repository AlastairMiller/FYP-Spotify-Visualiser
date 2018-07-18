package al.miller.sv.svas.service;

import al.miller.sv.ds.dao.RefinedAlbumDao;
import al.miller.sv.ds.dao.RefinedArtistDao;
import al.miller.sv.svas.MongoHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import refinedDataModels.RefinedArtist;
import refinedDataModels.RefinedTrack;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Service
public class ArtistService extends AbstractService<RefinedArtistDao,RefinedArtist> {

    private RefinedAlbumDao refinedAlbum;

    public ArtistService(MongoHelper mongoHelper) {
        super(mongoHelper);
        this.dao = mongoHelper.getRefinedArtistDao();
        this.refinedAlbum = mongoHelper.getRefinedAlbumDao();
    }

    public List<RefinedArtist> getForGenre(int offset, int limit, String genre) {
        return dao.getAllByGenre(offset, limit, genre);
    }

    public List<RefinedArtist> getMostFollowed(int offset, int limit) {
        return dao.getMostFollowedArtists(offset, limit);
    }

    public List<RefinedArtist> getArtistsOnAlbum(String albumId){
        List<String> artistIds = refinedAlbum.getAlbumArtistIds(albumId);
        return dao.getMultipleById(artistIds);
    }


}
