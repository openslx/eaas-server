package edu.kit.scc.webreg.dao;

import edu.kit.scc.webreg.entity.SerialEntity;

public interface SerialDao extends BaseDao<SerialEntity> {

	SerialEntity findByName(String name);

}
