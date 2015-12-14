package fi.vtt.dsp.service.serviceregistry.auth.resource;

import fi.vtt.dsp.service.serviceregistry.auth.ResourceCollectionAuthorization;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;

public class ServiceInstancesResourceAuthorization
		extends
			ResourceCollectionAuthorization {

	public ServiceInstancesResourceAuthorization() throws DAOGeneralSystemFault {
		super();
	}

	public ServiceInstancesResourceAuthorization set(String serviceId)
			throws DAOGeneralSystemFault {
		this.service = this.getExistingService(serviceId);
		return this;
	}

	protected boolean authorizeRegisteredUserCreate(String userId) {
		return true;
	}

	protected boolean authorizeRegisteredServiceCreate(String serviceId) {
		return this.isSame(service, serviceId);
	}
}
