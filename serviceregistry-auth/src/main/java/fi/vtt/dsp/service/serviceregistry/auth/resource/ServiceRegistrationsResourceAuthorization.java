package fi.vtt.dsp.service.serviceregistry.auth.resource;

import fi.vtt.dsp.service.serviceregistry.auth.ResourceCollectionAuthorization;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import org.springframework.security.core.Authentication;

public class ServiceRegistrationsResourceAuthorization
		extends
			ResourceCollectionAuthorization {

	public ServiceRegistrationsResourceAuthorization()
			throws DAOGeneralSystemFault {
		super();
	}

	protected boolean authorizeRegisteredUserCreate(String userId) {
		return true;
	}

	protected boolean authorizeRegisteredServiceCreate(String serviceId) {
		return true;
	}

	@Override
	public boolean canList(Authentication auth) {
		return true;
	}
}
