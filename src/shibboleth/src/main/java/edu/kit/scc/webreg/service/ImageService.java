package edu.kit.scc.webreg.service;

import edu.kit.scc.webreg.entity.ImageEntity;

public interface ImageService extends BaseService<ImageEntity> {

	ImageEntity findByName(String name);

	ImageEntity findByIdWithData(Long id);

}
