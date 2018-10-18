package edu.kit.scc.webreg.dao;

import java.util.List;
import edu.kit.scc.webreg.entity.BaseEntity;

public interface BaseDao<T extends BaseEntity> {

    T createNew();
    
    T persist(T entity);

    T merge(T entity);

    List<T> findAll();

    T findById(Long id);

    void delete(T entity);

}
