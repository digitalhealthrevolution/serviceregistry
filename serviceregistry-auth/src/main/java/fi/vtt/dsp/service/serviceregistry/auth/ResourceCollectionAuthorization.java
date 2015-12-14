package fi.vtt.dsp.service.serviceregistry.auth;

import org.springframework.security.core.Authentication;

import fi.vtt.dsp.service.serviceregistry.auth.token.Agent;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;

public abstract class ResourceCollectionAuthorization extends BaseAuthorization {

	public ResourceCollectionAuthorization() throws DAOGeneralSystemFault {
		super();
	}

	private static final boolean DEFAULT_AUTH_ADMIN_CREATE = true;
	private static final boolean DEFAULT_AUTH_REGISTERED_CREATE = false;
	private static final boolean DEFAULT_AUTH_GUEST_CREATE = false;

	protected boolean authorizeAdminUserCreate(String userId) {
		return DEFAULT_AUTH_ADMIN_CREATE;
	}
	protected boolean authorizeAdminServiceCreate(String serviceId) {
		return DEFAULT_AUTH_ADMIN_CREATE;
	}
	protected boolean authorizeAdminServiceInstanceCreate(
			String serviceInstanceId) {
		return DEFAULT_AUTH_ADMIN_CREATE;
	}

	protected boolean authorizeRegisteredUserCreate(String userId) {
		return DEFAULT_AUTH_REGISTERED_CREATE;
	}
	protected boolean authorizeRegisteredServiceCreate(String serviceId) {
		return DEFAULT_AUTH_REGISTERED_CREATE;
	}
	protected boolean authorizeRegisteredServiceInstanceCreate(
			String serviceInstanceId) {
		return DEFAULT_AUTH_REGISTERED_CREATE;
	}

	protected boolean authorizeGuestCreate() {
		return DEFAULT_AUTH_GUEST_CREATE;
	}

	public boolean canCreate(Authentication auth) {

		Agent agent = getAgent(auth);

		String agentId = agent.getId();
		boolean authorized = false;

		switch (agent.getRole()) {
			case ROLE_ADMIN :
				switch (agent.getType()) {
					case USER :
						authorized = authorizeAdminUserCreate(agentId);
						break;
					case SERVICE :
						authorized = authorizeAdminServiceCreate(agentId);
						break;
					case SERVICE_INSTANCE :
						authorized = authorizeAdminServiceInstanceCreate(agentId);
						break;
					default :
						break;
				}
				break;
			case ROLE_REGISTERED :
				switch (agent.getType()) {
					case USER :
						authorized = authorizeRegisteredUserCreate(agentId);
						break;
					case SERVICE :
						authorized = authorizeRegisteredServiceCreate(agentId);
						break;
					case SERVICE_INSTANCE :
						authorized = authorizeRegisteredServiceInstanceCreate(agentId);
						break;
					default :
						break;
				}
				break;
			case ROLE_GUEST :
				authorized = authorizeGuestCreate();
				break;
			default :
				break;
		}

		return authorized;
	}
}
