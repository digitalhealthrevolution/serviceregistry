package fi.vtt.dsp.service.serviceregistry.impl.dao;

import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;

public interface ServiceRegistryDAO {

	List<ServiceRegistryEntry> getAll() throws DAOGeneralSystemFault;

	List<ServiceRegistryEntry> findAll(
			Set<Entry<String, List<String>>> queryParams, int top, int tail,
			int startSection, int endSection, String userId)
			throws DAOGeneralSystemFault;

	List<ServiceRegistryEntry> findAll(
			Set<Entry<String, List<String>>> queryParams, int top, int tail,
			int startSection, int endSection) throws DAOGeneralSystemFault;

	ServiceRegistryEntry findServiceRegistryEntry(String serviceId)
			throws DAOGeneralSystemFault, DAONotFoundFault;

	String insertNewServiceRegistryEntry(ServiceRegistryEntry sRegEntr)
			throws DAOGeneralSystemFault, DAONotSavedFault, DAONotFoundFault,
			DAOUpdateFailedFault;

	String updateServiceRegistryEntry(String serviceId,
			ServiceRegistryEntry sRegEntr) throws DAOGeneralSystemFault,
			DAOUpdateFailedFault, DAONotFoundFault, DAONotSavedFault;

	void deleteServiceRegistryEntry(String serviceId)
			throws DAOGeneralSystemFault, DAONotFoundFault;
}
