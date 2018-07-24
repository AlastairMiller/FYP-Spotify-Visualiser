package al.miller.sv.svas.service;

import al.miller.sv.ds.dao.RefinedAlbumDao;
import al.miller.sv.ds.util.SortOrder;
import al.miller.sv.svas.MongoHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import refinedDataModels.RefinedAlbum;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class AlbumService extends AbstractService<RefinedAlbumDao, RefinedAlbum> {

    public AlbumService(MongoHelper mongoHelper) {
        super(mongoHelper);
        this.dao = mongoHelper.getRefinedAlbumDao();
    }

    public List<RefinedAlbum> getAllFromArtistId(int offset, int limit, String artistId) {
        return dao.getAllFromArtistId(offset, limit, artistId);
    }

    public List<RefinedAlbum> getAllContainingTrack(int offset, int limit, String trackId) {
        return dao.getAllFromTrackId(offset, limit, trackId);
    }

    public List<RefinedAlbum> getByReleaseDate(int offset, int limit, SortOrder sortOrder) {
        return dao.getByReleaseDate(offset, limit, sortOrder);
    }

    public List<RefinedAlbum> getAlbumsFromDate(int offset, int limit, Date date) {
        return dao.getAlbumsFromDate(offset, limit, date);
    }

}
