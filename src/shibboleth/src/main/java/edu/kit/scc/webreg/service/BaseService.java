package edu.kit.scc.webreg.service;

import java.io.Serializable;
import java.util.List;
import edu.kit.scc.webreg.entity.BaseEntity;

public interface BaseService<T extends BaseEntity> extends Serializable {

    T createNew();

    T save(T entity);

    void delete(T entity);

    List<T> findAll();

    T findById(Long id);

}