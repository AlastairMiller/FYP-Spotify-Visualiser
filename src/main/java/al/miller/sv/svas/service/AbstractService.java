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
public abstract class AbstractService<T extends AbstractDao> {

    protected T dao;
    protected MongoHelper mongoHelper;

    @Autowired
    public AbstractService(MongoHelper mongoHelper) {
        this.mongoHelper = mongoHelper;
    }

    public List<Object> getMostPopular(int limit, int offset) {
        return dao.getMostPopular(limit, offset);
    }

    public Object getById(String id) {
        return dao.getById(id);
    }

    public Object getBySpotifyUri(String uri) {
        return dao.getBySpotifyUri(uri);
    }

    public List<Object> getMultipleById(List<String> ids) {
        return dao.getMultipleById(ids);
    }

    public List<Object> getAllMatchingName(String name) {
        return dao.getAllMatchingNames(name);
    }

}
