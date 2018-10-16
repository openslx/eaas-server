package edu.kit.scc.webreg.service;

import java.util.List;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public interface RegistryService extends BaseService<RegistryEntity> {

	List<RegistryEntity> findByService(ServiceEntity service);

	List<RegistryEntity> findAllByStatus(RegistryStatus status);

	List<RegistryEntity> findByServiceAndStatus(ServiceEntity service,
			RegistryStatus status);

	List<RegistryEntity> findByUserAndStatus(UserEntity user,
			RegistryStatus status);

	List<RegistryEntity> findByServiceAndUser(ServiceEntity service, UserEntity user);

	RegistryEntity findByServiceAndUserAndStatus(ServiceEntity service,
			UserEntity user, RegistryStatus status);
	
}
