package fi.vtt.dsp.service.serviceregistry.auth.resource;

import fi.vtt.dsp.service.serviceregistry.auth.ResourceAuthorization;
import fi.vtt.dsp.service.serviceregistry.auth.token.Agent;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import org.springframework.security.core.Authentication;

public class ServiceRegistrationResourceAuthorization
		extends
			ResourceAuthorization {
	public ServiceRegistrationResourceAuthorization()
			throws DAOGeneralSystemFault {
		super();
	}

	public ServiceRegistrationResourceAuthorization set(String serviceId)
			throws DAOGeneralSystemFault {
		service = this.getExistingService(serviceId);
		return this;
	}

	protected boolean authorizeRegisteredUserModify(String userId) {
		return isUserAuthorizedToModifyService(userId, service);
	}

	protected boolean authorizeRegisteredServiceModify(String serviceId) {
		return this.isSame(service, serviceId);
	}

	public boolean canGet(Authentication auth) {
		if (isResourceNotFound()) {
			return true;
		}

		Agent agent = getAgent(auth);
		String agentId = agent.getId();

		if (agent.getRole() == AgentRole.ROLE_ADMIN) {
			return true;
		}

		if (agent.getRole() == AgentRole.ROLE_GUEST) {
			return isUserAuthorizedToAccessService(service);
		}

		if (agent.getRole() == AgentRole.ROLE_REGISTERED) {
			if (agent.getType() == AgentType.SERVICE) {
				if (service.getServiceId().equals(agentId)) {
					return true;
				}

				return false;
			} else if (agent.getType() == AgentType.SERVICE_INSTANCE) {
				return false;
			} else if (agent.getType() == AgentType.UNDEFINED) {
				return false;
			} else if (agent.getType() == AgentType.USER) {
				return isUserAuthorizedToAccessService(agentId, service);
			}
		}

		return false;
	}

	@Override
	public boolean canUpdate(Authentication auth) throws DAOGeneralSystemFault,
			DAONotFoundFault {
		if (isResourceNotFound()) {
			return true;
		}

		Agent agent = getAgent(auth);
		String agentId = agent.getId();

		if (agent.getRole() == AgentRole.ROLE_ADMIN) {
			return true;
		}

		if (agent.getRole() == AgentRole.ROLE_GUEST) {
			return false;
		}

		if (agent.getRole() == AgentRole.ROLE_REGISTERED) {
			if (agent.getType() == AgentType.SERVICE) {
				if (service.getServiceId().equals(agentId)) {
					return true;
				}

				return false;
			} else if (agent.getType() == AgentType.SERVICE_INSTANCE) {
				return false;
			} else if (agent.getType() == AgentType.UNDEFINED) {
				return false;
			} else if (agent.getType() == AgentType.USER) {
				return isUserAuthorizedToModifyService(agentId, service);
			}
		}

		return false;
	}

	@Override
	public boolean canDelete(Authentication auth) throws DAOGeneralSystemFault,
			DAONotFoundFault {
		if (isResourceNotFound()) {
			return true;
		}

		Agent agent = getAgent(auth);
		String agentId = agent.getId();

		if (agent.getRole() == AgentRole.ROLE_ADMIN) {
			return true;
		}

		if (agent.getRole() == AgentRole.ROLE_GUEST) {
			return false;
		}

		if (agent.getRole() == AgentRole.ROLE_REGISTERED) {
			if (agent.getType() == AgentType.SERVICE) {
				if (service.getServiceId().equals(agentId)) {
					return true;
				}

				return false;
			} else if (agent.getType() == AgentType.SERVICE_INSTANCE) {
				return false;
			} else if (agent.getType() == AgentType.UNDEFINED) {
				return false;
			} else if (agent.getType() == AgentType.USER) {
				return isUserAuthorizedToModifyService(agentId, service);
			}
		}

		return false;
	}
}
