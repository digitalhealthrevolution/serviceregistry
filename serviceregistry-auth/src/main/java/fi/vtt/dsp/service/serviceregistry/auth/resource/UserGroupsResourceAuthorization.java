package fi.vtt.dsp.service.serviceregistry.auth.resource;

import fi.vtt.dsp.service.serviceregistry.auth.ResourceCollectionAuthorization;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.common.UserGroup;

public class UserGroupsResourceAuthorization
		extends
			ResourceCollectionAuthorization {
	public UserGroupsResourceAuthorization() throws DAOGeneralSystemFault {
		super();
	}

	protected boolean authorizeRegisteredUserCreate(String userId) {
		return true;
	}
}
