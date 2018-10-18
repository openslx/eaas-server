package edu.kit.scc.webreg.dao;

import java.util.List;
import edu.kit.scc.webreg.entity.ImageEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;

public interface ServiceDao extends BaseDao<ServiceEntity> {

	ServiceEntity findWithPolicies(Long id);

	List<ServiceEntity> findAllWithPolicies();

	List<ServiceEntity> findAllByImage(ImageEntity image);

	ServiceEntity findByIdWithServiceProps(Long id);

	List<ServiceEntity> findByAdminRole(RoleEntity role);

	List<ServiceEntity> findByApproverRole(RoleEntity role);

	ServiceEntity findByShortName(String shortName);

	List<ServiceEntity> findAllPublishedWithServiceProps();

}
