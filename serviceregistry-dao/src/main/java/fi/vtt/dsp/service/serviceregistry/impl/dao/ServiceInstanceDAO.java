package fi.vtt.dsp.service.serviceregistry.impl.dao;

import fi.vtt.dsp.service.serviceregistry.common.instance.Availability;
import fi.vtt.dsp.service.serviceregistry.common.instance.AvailabilityRequestEndPoint;
import fi.vtt.dsp.service.serviceregistry.common.instance.BindingRequestEndPoint;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.serviceframework.common.Binding;

public interface ServiceInstanceDAO {

	ServiceInstance findServiceInstance(String serviceId, String instanceId)
			throws DAOGeneralSystemFault, DAONotFoundFault;

	String insertNewServiceInstance(String serviceId, ServiceInstance servInst)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotSavedFault, DAONotFoundFault;

	String updateServiceInstance(String serviceId, String servDepId,
			ServiceInstance servInst) throws DAOGeneralSystemFault,
			DAOUpdateFailedFault, DAONotFoundFault, DAONotSavedFault;

	String deleteServiceInstance(String serviceId, String servInstId)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotFoundFault;

	AvailabilityRequestEndPoint findAvailabilityRequestEndPoint(
			String serviceId, String instanceId) throws DAOGeneralSystemFault,
			DAONotFoundFault;

	String updateAvailabilityRequestEndPoint(String serviceId,
			String instanceId, AvailabilityRequestEndPoint avaEP)
			throws DAOGeneralSystemFault, DAOUpdateFailedFault,
			DAONotFoundFault, DAONotSavedFault;

	String deleteAvailabilityRequestEndPoint(String serviceId, String instanceId)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotFoundFault, DAONotSavedFault;

	BindingRequestEndPoint findBindingRequestEndPoint(String serviceId,
			String instanceId) throws DAOGeneralSystemFault, DAONotFoundFault;

	String updateBindingRequestEndPoint(String serviceId, String instanceId,
			BindingRequestEndPoint bindEP) throws DAOGeneralSystemFault,
			DAOUpdateFailedFault, DAONotFoundFault, DAONotSavedFault;

	String deleteBindingRequestEndPoint(String serviceId, String instanceId)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotFoundFault, DAONotSavedFault;

	ServiceAccessEndPoint findServiceAccessEndPoint(String serviceId,
			String instanceId) throws DAOGeneralSystemFault, DAONotFoundFault;

	String updateServiceAccessEndPoint(String serviceId, String instanceId,
			ServiceAccessEndPoint saEP) throws DAOGeneralSystemFault,
			DAOUpdateFailedFault, DAONotFoundFault, DAONotSavedFault;

	String deleteServiceAccessEndPoint(String serviceId, String instanceId)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotFoundFault, DAONotSavedFault;

	Binding findBinding(String serviceId, String instanceId, String bindingId)
			throws DAOGeneralSystemFault, DAONotFoundFault;

	String insertNewBinding(String serviceId, String instanceId,
			Binding newBinding) throws DAOUpdateFailedFault,
			DAOGeneralSystemFault, DAONotSavedFault, DAONotFoundFault;

	String updateBinding(String serviceId, String instanceId, String bindingId,
			Binding updatedBinding) throws DAOGeneralSystemFault,
			DAOUpdateFailedFault, DAONotFoundFault, DAONotSavedFault;

	String deleteBinding(String serviceId, String instanceId, String bindingId)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotFoundFault, DAONotSavedFault;

	Availability findAvailability(String serviceId, String instanceId)
			throws DAOGeneralSystemFault, DAONotFoundFault;

	String updateAvailability(String serviceId, String instanceId,
			Availability servAvailability) throws DAOGeneralSystemFault,
			DAOUpdateFailedFault, DAONotFoundFault, DAONotSavedFault;

	String deleteAvailability(String serviceId, String instanceId)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotFoundFault, DAONotSavedFault;
}
