package al.miller.sv.svas.service;

import al.miller.sv.ds.dao.AbstractDao;
import al.miller.sv.svas.MongoHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public abstract class AbstractService<T extends AbstractDao,S> {

    T dao;

    @Autowired
    public AbstractService() {
    }

    public List<S> getMostPopular(int offset, int limit) {
        return dao.getMostPopular(limit, offset);
    }

    public S getById(String id) {
        return (S) dao.getById(id);
    }

    public S getBySpotifyUri(String uri) {
        return (S) dao.getBySpotifyUri(uri);
    }

    public List<S> getMultipleById(List<String> ids) {
        return dao.getMultipleById(ids);
    }

    public List<S> getAllMatchingName(String name) {
        return dao.getAllMatchingNames(name);
    }

}
