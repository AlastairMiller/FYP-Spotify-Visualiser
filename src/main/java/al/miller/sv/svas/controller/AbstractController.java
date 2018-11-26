package al.miller.sv.svas.controller;

import al.miller.sv.svas.service.AbstractService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@AllArgsConstructor(onConstructor = @__(@Autowired))
public abstract class AbstractController<T> {

    protected abstract AbstractService getService();

    protected abstract T getById(String id);

    protected abstract List<T> getMultipleById(String ids);

    protected abstract List<T> search(String searchTerm, int offset, int limit);

    protected abstract List<T> getMostPopular(int limit, int offset);

    void inputValidation(int offset, int limit) throws IllegalArgumentException {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be greater than 0, but was " + limit);
        }
        if (limit > offset+ 25) {
            throw new IllegalArgumentException("Limit cannot be greater then 50, but was " + limit);
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must be 0 or greater, but was " + offset);
        }
    }
}
