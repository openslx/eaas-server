package edu.kit.scc.webreg.service;

import edu.kit.scc.webreg.entity.SerialEntity;

public interface SerialService extends BaseService<SerialEntity> {

	SerialEntity findByName(String name);

	void createIfNotExistant(String name, Long initalValue);

	Long next(String name);

}
