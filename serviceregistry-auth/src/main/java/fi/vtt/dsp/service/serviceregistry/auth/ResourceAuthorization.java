package fi.vtt.dsp.service.serviceregistry.auth;

import org.springframework.security.core.Authentication;

import fi.vtt.dsp.service.serviceregistry.auth.token.Agent;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import java.util.logging.Logger;

public abstract class ResourceAuthorization extends BaseAuthorization {
	private static final Logger LOGGER = Logger
			.getLogger(ResourceAuthorization.class.getName());
	private static final boolean DEFAULT_AUTH_ADMIN_MODIFY = true;
	private static final boolean DEFAULT_AUTH_ADMIN_UPDATE = true;
	private static final boolean DEFAULT_AUTH_ADMIN_DELETE = true;

	private static final boolean DEFAULT_AUTH_REGISTERED_MODIFY = false;
	private static final boolean DEFAULT_AUTH_REGISTERED_UPDATE = false;
	private static final boolean DEFAULT_AUTH_REGISTERED_DELETE = false;

	private static final boolean DEFAULT_AUTH_GUEST_MODIFY = false;
	private static final boolean DEFAULT_AUTH_GUEST_UPDATE = false;
	private static final boolean DEFAULT_AUTH_GUEST_DELETE = false;

	public ResourceAuthorization() throws DAOGeneralSystemFault {

	}

	protected boolean authorizeAdminUserModify(String userId) {
		return DEFAULT_AUTH_ADMIN_MODIFY;
	}
	protected boolean authorizeAdminServiceModify(String serviceId) {
		return DEFAULT_AUTH_ADMIN_MODIFY;
	}
	protected boolean authorizeAdminServiceInstanceModify(
			String serviceInstanceId) {
		return DEFAULT_AUTH_ADMIN_MODIFY;
	}
	protected boolean authorizeAdminUserUpdate(String userId) {
		return authorizeAdminUserModify(userId) || DEFAULT_AUTH_ADMIN_UPDATE;
	}
	protected boolean authorizeAdminServiceUpdate(String serviceId) {
		return authorizeAdminServiceModify(serviceId)
				|| DEFAULT_AUTH_ADMIN_UPDATE;
	}
	protected boolean authorizeAdminServiceInstanceUpdate(
			String serviceInstanceId) {
		return authorizeAdminServiceInstanceModify(serviceInstanceId)
				|| DEFAULT_AUTH_ADMIN_UPDATE;
	}
	protected boolean authorizeAdminUserDelete(String userId) {
		return authorizeAdminUserModify(userId) || DEFAULT_AUTH_ADMIN_DELETE;
	}
	protected boolean authorizeAdminServiceDelete(String serviceId) {
		return authorizeAdminServiceModify(serviceId)
				|| DEFAULT_AUTH_ADMIN_DELETE;
	}
	protected boolean authorizeAdminServiceInstanceDelete(
			String serviceInstanceId) {
		return authorizeAdminServiceInstanceModify(serviceInstanceId)
				|| DEFAULT_AUTH_ADMIN_DELETE;
	}

	protected boolean authorizeRegisteredUserModify(String userId) {
		return DEFAULT_AUTH_REGISTERED_MODIFY;
	}
	protected boolean authorizeRegisteredServiceModify(String serviceId) {
		return DEFAULT_AUTH_REGISTERED_MODIFY;
	}
	protected boolean authorizeRegisteredServiceInstanceModify(
			String serviceInstanceId) {
		return DEFAULT_AUTH_REGISTERED_MODIFY;
	}
	protected boolean authorizeRegisteredUserUpdate(String userId) {
		return authorizeRegisteredUserModify(userId)
				|| DEFAULT_AUTH_REGISTERED_UPDATE;
	}
	protected boolean authorizeRegisteredServiceUpdate(String serviceId) {
		return authorizeRegisteredServiceModify(serviceId)
				|| DEFAULT_AUTH_REGISTERED_UPDATE;
	}
	protected boolean authorizeRegisteredServiceInstanceUpdate(
			String serviceInstanceId) {
		return authorizeRegisteredServiceInstanceModify(serviceInstanceId)
				|| DEFAULT_AUTH_REGISTERED_UPDATE;
	}
	protected boolean authorizeRegisteredUserDelete(String userId) {
		return authorizeRegisteredUserModify(userId)
				|| DEFAULT_AUTH_REGISTERED_DELETE;
	}
	protected boolean authorizeRegisteredServiceDelete(String serviceId) {
		return authorizeRegisteredServiceModify(serviceId)
				|| DEFAULT_AUTH_REGISTERED_DELETE;
	}
	protected boolean authorizeRegisteredServiceInstanceDelete(
			String serviceInstanceId) {
		return authorizeRegisteredServiceInstanceModify(serviceInstanceId)
				|| DEFAULT_AUTH_REGISTERED_DELETE;
	}

	protected boolean authorizeGuestModify() {
		return DEFAULT_AUTH_GUEST_MODIFY;
	}
	protected boolean authorizeGuestUpdate() {
		return authorizeGuestModify() || DEFAULT_AUTH_GUEST_UPDATE;
	}
	protected boolean authorizeGuestDelete() {
		return authorizeGuestModify() || DEFAULT_AUTH_GUEST_DELETE;
	}

	private boolean authorizeAdminUpdate(AgentType type, String agentId) {
		boolean authorized = false;
		switch (type) {
			case USER :
				authorized = authorizeAdminUserUpdate(agentId);
				break;
			case SERVICE :
				authorized = authorizeAdminServiceUpdate(agentId);
				break;
			case SERVICE_INSTANCE :
				authorized = authorizeAdminServiceInstanceUpdate(agentId);
				break;
			default :
				break;
		}
		return authorized;
	}

	private boolean authorizeRegisteredUpdate(AgentType type, String agentId) {
		boolean authorized = false;
		switch (type) {
			case USER :
				authorized = authorizeRegisteredUserUpdate(agentId);
				break;
			case SERVICE :
				authorized = authorizeRegisteredServiceUpdate(agentId);
				break;
			case SERVICE_INSTANCE :
				authorized = authorizeRegisteredServiceInstanceUpdate(agentId);
				break;
			default :
				break;
		}
		return authorized;
	}

	protected boolean authorizeAdminDelete(AgentType type, String agentId) {
		boolean authorized = false;
		switch (type) {
			case USER :
				authorized = authorizeAdminUserDelete(agentId);
				break;
			case SERVICE :
				authorized = authorizeAdminServiceDelete(agentId);
				break;
			case SERVICE_INSTANCE :
				authorized = authorizeAdminServiceInstanceDelete(agentId);
				break;
			default :
				break;
		}
		return authorized;
	}

	private boolean authorizeRegisteredDelete(AgentType type, String agentId) {
		boolean authorized = false;
		switch (type) {
			case USER :
				authorized = authorizeRegisteredUserDelete(agentId);
				break;
			case SERVICE :
				authorized = authorizeRegisteredServiceDelete(agentId);
				break;
			case SERVICE_INSTANCE :
				authorized = authorizeRegisteredServiceInstanceDelete(agentId);
				break;
			default :
				break;
		}
		return authorized;
	}

	public boolean canUpdate(Authentication auth) throws DAOGeneralSystemFault,
			DAONotFoundFault {

		if (isResourceNotFound()) {
			return true;
		}

		Agent agent = getAgent(auth);
		String agentId = agent.getId();

		boolean authorized = false;

		switch (agent.getRole()) {
			case ROLE_ADMIN :
				authorized = authorizeAdminUpdate(agent.getType(), agentId);
				break;
			case ROLE_REGISTERED :
				authorized = authorizeRegisteredUpdate(agent.getType(), agentId);
				break;
			case ROLE_GUEST :
				authorized = authorizeGuestUpdate();
				break;
			default :
				break;
		}

		return authorized;
	}

	public boolean canDelete(Authentication auth) throws DAOGeneralSystemFault,
			DAONotFoundFault {

		if (isResourceNotFound()) {
			return true;
		}

		Agent agent = getAgent(auth);
		String agentId = agent.getId();

		boolean authorized = false;

		switch (agent.getRole()) {
			case ROLE_ADMIN :
				authorized = authorizeAdminDelete(agent.getType(), agentId);
				break;
			case ROLE_REGISTERED :
				authorized = isUserAuthorizedToModifyService(agentId, service);
				break;
			case ROLE_GUEST :
				authorized = authorizeGuestDelete();
				break;
			default :
				break;
		}

		return authorized;
	}
}
