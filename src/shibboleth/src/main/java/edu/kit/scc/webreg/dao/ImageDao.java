package edu.kit.scc.webreg.dao;

import edu.kit.scc.webreg.entity.ImageEntity;

public interface ImageDao extends BaseDao<ImageEntity> {

	ImageEntity findByName(String name);

	ImageEntity findByIdWithData(Long id);
}
