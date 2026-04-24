package services;

import java.util.List;

public interface Service<T, ID> {
    T findEntity(ID id) throws Exception;
    void saveEntity(T entity) throws Exception;
    void deleteEntity(ID id) throws Exception;
    void updateEntity(T entity) throws Exception;
    List<T> findAllEntities() throws Exception;
}