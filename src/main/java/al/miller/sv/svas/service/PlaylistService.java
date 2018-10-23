package al.miller.sv.svas.service;

import al.miller.sv.ds.dao.RefinedArtistDao;
import al.miller.sv.ds.dao.RefinedPlaylistDao;
import al.miller.sv.ds.dao.RefinedTrackDao;
import al.miller.sv.svas.MongoHelper;
import com.mongodb.client.MongoCollection;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Service;
import refinedDataModels.RefinedArtist;
import refinedDataModels.RefinedPlaylist;
import refinedDataModels.RefinedTrack;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlaylistService extends AbstractService<RefinedPlaylistDao, RefinedPlaylist> {

    private RefinedTrackDao refinedTrackDao;
    private RefinedArtistDao refinedArtistDao;

    public PlaylistService(MongoHelper mongoHelper) {
        super();
        this.dao = mongoHelper.getRefinedPlaylistDao();
        this.refinedTrackDao = mongoHelper.getRefinedTrackDao();
        this.refinedArtistDao = mongoHelper.getRefinedArtistDao();
    }

    public List<RefinedPlaylist> getByUserId(String userId, int offset, int limit) {
        return dao.getByUserId(userId, offset, limit);
    }

    public List<RefinedPlaylist> getAllPlaylistsContainingSong(int offset, int limit, String id) {
        return dao.getPlaylistsContainingTrack(id, offset, limit);
    }

    public List<RefinedPlaylist> getPlaylistsForCountry(String countryCode, int offset, int limit) {
        return dao.getPlaylistByCountry(countryCode, offset, limit);
    }

    public List<Document> getMostPopularSongsForCountry(String countryCode, int offset, int limit) {
        return dao.getMostPopularTracks(countryCode, offset, limit);
    }

    public List<Document> getMostPopularGenresForCountry(String countryCode, int offset, int limit) {
        dao.preformPopularTrackMapReduce(countryCode);

        if (!dao.getMostPopularGenresTimeStamp().containsKey(countryCode) || dao.getMostPopularGenresTimeStamp().get(countryCode).isBefore(LocalDateTime.now().minusDays(1))) {
            List<Document> documents = getMostPopularArtistsForCountry(countryCode, 0, 0);
            //Get Artist Ids from Tracks
            List<String> arrayList = documents.stream()
                    .map(document -> document.getString("_id"))
                    .collect(Collectors.toList());

            List<RefinedArtist> refinedArtists = refinedArtistDao.getMultipleById(arrayList);
            Map<String, Float> genrePopularityMap = new HashMap<>();

            for (RefinedArtist refinedArtist : refinedArtists) {
                for (Document document : documents) {
                    if (document.getString("_id").equals(refinedArtist.getId())) {
                        for (String genre : refinedArtist.getGenres()) {
                            if (genrePopularityMap.containsKey(genre)) {
                                genrePopularityMap.put(genre, genrePopularityMap.get(genre) + Float.valueOf(document.get("value").toString()));
                            } else {
                                genrePopularityMap.put(genre, Float.valueOf(document.get("value").toString()));
                            }
                        }
                    }
                }
            }
            List<Document> genrePopularityDocuments = new ArrayList<>();

            Iterator<Map.Entry<String, Float>> mapIterable = genrePopularityMap.entrySet().iterator();
            while (mapIterable.hasNext()) {
                Map.Entry<String, Float> mapEntry = mapIterable.next();
                Map<String, Object> documentMap = new HashMap<>();
                documentMap.put("_id", mapEntry.getKey());
                documentMap.put("value", mapEntry.getValue());
                genrePopularityDocuments.add(new Document(documentMap));
                mapIterable.remove();
            }

            if (!dao.getClientHandler().getMongoDB().listCollectionNames().into(new ArrayList<>()).contains("genres_popular_in_" + countryCode)) {
                dao.createCollection("genres_popular_in_" + countryCode);
            } else {
                dao.getClientHandler().getMongoDB().getCollection("genres_popular_in_" + countryCode).drop();
            }
            MongoCollection<Document> collection = dao.getClientHandler().getMongoDB().getCollection("genres_popular_in_"+ countryCode);
            collection.insertMany(genrePopularityDocuments);

            Map<String, LocalDateTime> timeStamps = dao.getMostPopularGenresTimeStamp();
            timeStamps.put(countryCode, LocalDateTime.now());
            dao.setMostPopularGenresTimeStamp(timeStamps);

        }

        return refinedArtistDao.getMostPopularGenresForCountry(countryCode, limit, offset);
    }


    public List<Document> getMostPopularArtistsForCountry(String countryCode, int offset, int limit) {
        dao.preformPopularTrackMapReduce(countryCode);

        if (!dao.getMostPopularArtistsTimeStamp().containsKey(countryCode) || dao.getMostPopularArtistsTimeStamp().get(countryCode).isBefore(LocalDateTime.now().minusDays(1))) {
            ArrayList<Document> documents = dao.getClientHandler().getMongoDB().getCollection("tracks_popular_in_" + countryCode).find().into(new ArrayList<>());
            //Get Artist Ids from Tracks
            List<String> arrayList = documents.stream()
                    .map(document -> document.getString("_id"))
                    .collect(Collectors.toList());

            //Get TrackIds
            List<RefinedTrack> refinedTracks = refinedTrackDao.getMultipleById(arrayList);
            Map<String, Float> artistPopularityMap = new HashMap<>();

            for (RefinedTrack refinedTrack : refinedTracks) {
                for (Document document : documents) {

                    if (document.getString("_id").equals(refinedTrack.getId())) {
                        for (String artistId : refinedTrack.getArtistIds()) {
                            if (artistPopularityMap.containsKey(artistId)) {
                                artistPopularityMap.put(artistId, artistPopularityMap.get(artistId) + Float.valueOf(document.get("value").toString()));
                            } else {
                                artistPopularityMap.put(artistId, Float.valueOf(document.get("value").toString()));
                            }
                        }

                    }
                }
            }
            List<Document> artistPopularityDocuments = new ArrayList<>();

            Iterator<Map.Entry<String, Float>> mapIterable = artistPopularityMap.entrySet().iterator();
            while (mapIterable.hasNext()) {
                Map.Entry<String, Float> mapEntry = mapIterable.next();
                Map<String, Object> documentMap = new HashMap<>();
                documentMap.put("_id", mapEntry.getKey());
                documentMap.put("value", mapEntry.getValue());
                artistPopularityDocuments.add(new Document(documentMap));
                mapIterable.remove();
            }

            if (!dao.getClientHandler().getMongoDB().listCollectionNames().into(new ArrayList<>()).contains("artists_popular_in_" + countryCode)) {
                dao.createCollection("artists_popular_in_" + countryCode);
            } else {
                dao.getClientHandler().getMongoDB().getCollection("artists_popular_in_" + countryCode).drop();
            }

            MongoCollection<Document> collection = dao.getClientHandler().getMongoDB().getCollection("artists_popular_in_" + countryCode);
            collection.insertMany(artistPopularityDocuments);

            Map<String, LocalDateTime> timeStamps = dao.getMostPopularArtistsTimeStamp();
            timeStamps.put(countryCode, LocalDateTime.now());
            dao.setMostPopularArtistsTimeStamp(timeStamps);
        }

        if (offset == 0 && limit == 0) {
            return refinedArtistDao.getMostPopularArtistsForCountry(countryCode);
        } else {
            return refinedArtistDao.getMostPopularArtistsForCountry(countryCode, offset, limit);
        }

    }
}
