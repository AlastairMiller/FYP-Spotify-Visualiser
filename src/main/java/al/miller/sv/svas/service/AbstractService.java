package al.miller.sv.svas.service;

import al.miller.sv.ds.dao.AbstractDao;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@AllArgsConstructor(onConstructor = @__(@Autowired))
public abstract class AbstractService<T> {

    protected AbstractDao<T> dao;

    public List<T> getMostPopular(int limit, int offset) {
        return dao.getMostPopular(limit, offset);
    }

    public T getById(String id) {
        return dao.getById(id);
    }

    public T getBySpotifyUri(String uri){
        return dao.getBySpotifyUri(uri);
    }

    public List<T> getMultipleById(List<String> ids){
        return dao.getMultipleById(ids);
    }

    public List<T> getAllMatchingName(String name){
        return dao.getAllMatchingNames(name);
    }

    public List<T>


}
