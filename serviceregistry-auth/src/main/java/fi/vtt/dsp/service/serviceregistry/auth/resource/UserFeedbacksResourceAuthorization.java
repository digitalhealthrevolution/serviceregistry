package fi.vtt.dsp.service.serviceregistry.auth.resource;

import fi.vtt.dsp.service.serviceregistry.auth.ResourceCollectionAuthorization;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;

public class UserFeedbacksResourceAuthorization
		extends
			ResourceCollectionAuthorization {
	public UserFeedbacksResourceAuthorization() throws DAOGeneralSystemFault {
		super();
	}

	public UserFeedbacksResourceAuthorization set(String serviceId)
			throws DAOGeneralSystemFault {
		service = this.getExistingService(serviceId);
		return this;
	}

	protected boolean authorizeRegisteredUserCreate(String userId) {
		// cannot give feedback to your own service
		return !this.isCreatedByUser(service, userId);
	}

}
