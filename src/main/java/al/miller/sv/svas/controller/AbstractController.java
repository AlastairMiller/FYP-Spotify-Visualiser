package al.miller.sv.svas.controller;

import al.miller.sv.svas.service.AbstractService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@AllArgsConstructor(onConstructor = @__(@Autowired))
public abstract class AbstractController<T> {

    protected abstract AbstractService<T> getService();

    @RequestMapping(path = "/{id}", method = GET)
    public T getById(@PathVariable("id") String id) {
        T t = getService().getById(id);
        return t;
    }

    @RequestMapping(path = "/mostPopular", method = GET)
    public List<T> getMostPopular(int limit, int offset) {
        inputValidation(offset,limit);
        return getService().getMostPopular(limit, offset);
    }

     void inputValidation(int offset, int limit) throws IllegalArgumentException {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be greater than 0, but was " + limit);
        }
        if (limit > 50) {
            throw new IllegalArgumentException("Limit cannot be greater then 50, but was " + limit);
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must be 0 or greater, but was " + offset);
        }
    }

}
