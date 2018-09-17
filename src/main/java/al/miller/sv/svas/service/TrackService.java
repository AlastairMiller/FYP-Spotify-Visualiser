package al.miller.sv.svas.service;

import al.miller.sv.ds.dao.RefinedPlaylistDao;
import al.miller.sv.ds.dao.RefinedTrackDao;
import al.miller.sv.svas.MongoHelper;
import com.neovisionaries.i18n.CountryCode;
import org.springframework.stereotype.Service;
import refinedDataModels.RefinedPlaylist;
import refinedDataModels.RefinedTrack;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TrackService extends AbstractService<RefinedTrackDao, RefinedTrack> {

    private RefinedPlaylistDao refinedPlaylistDao;

    public TrackService(MongoHelper mongoHelper) {
        super();
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
        if (limit > refinedPlaylist.getTrackIds().size()) limit = refinedPlaylist.getTrackIds().size();
        if (offset != limit) {
            List<String> trackIds = refinedPlaylist.getTrackIds().subList(offset, limit);
            return dao.getMultipleById(trackIds);
        } else {
            return null;
        }
    }

    public RefinedTrack getRandom() {
        return dao.getRandom();
    }

    public CountryCode mostPopularIn(String trackId) {
        int offset = 0, limit = 25;
        List<RefinedPlaylist> playlists = new ArrayList<>();
        while (playlists.size() == offset) {
            playlists = Stream.of(playlists, refinedPlaylistDao.getPlaylistsContainingTrack(trackId, offset, limit))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            offset += 25;
            limit += 25;
        }
        return playlists.stream()
                .map(RefinedPlaylist::getCountryCode).filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(CountryCode.UNDEFINED);
    }
}
