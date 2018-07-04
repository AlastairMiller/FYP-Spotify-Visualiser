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
        return null;
    }
}
