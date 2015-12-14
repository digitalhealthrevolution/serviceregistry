package fi.vtt.dsp.service.serviceregistry.auth.resource;

import fi.vtt.dsp.service.serviceregistry.auth.ResourceAuthorization;
import fi.vtt.dsp.service.serviceregistry.auth.token.Agent;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import org.springframework.security.core.Authentication;

public class ServiceInstanceResourceAuthorization extends ResourceAuthorization {

	public ServiceInstanceResourceAuthorization() throws DAOGeneralSystemFault {
		super();
	}

	public ServiceInstanceResourceAuthorization set(String serviceId,
			String serviceInstanceId) throws DAOGeneralSystemFault {
		service = this.getExistingService(serviceId);
		instance = this
				.getExistingServiceInstance(serviceId, serviceInstanceId);
		return this;
	}

	protected boolean authorizeRegisteredUserModify(String userId) {
		return isUserAuthorizedToModifyInstance(userId);
	}

	protected boolean authorizeRegisteredServiceModify(String serviceId) {
		return this.isSame(service, serviceId);
	}

	protected boolean authorizeRegisteredServiceInstanceModify(
			String serviceInstanceId) {
		return this.isSame(instance, serviceInstanceId);
	}

	public boolean canGet(Authentication auth) throws DAOGeneralSystemFault,
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
				if (instance.getServiceInstanceId().equals(agentId)) {
					return true;
				}

				return false;
			} else if (agent.getType() == AgentType.UNDEFINED) {
				return false;
			} else if (agent.getType() == AgentType.USER) {
				return isUserAuthorizedToAccessInstance(agentId);
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
				if (instance.getServiceInstanceId().equals(agentId)) {
					return true;
				}

				return false;
			} else if (agent.getType() == AgentType.UNDEFINED) {
				return false;
			} else if (agent.getType() == AgentType.USER) {
				return isUserAuthorizedToModifyInstance(agentId);
			}
		}

		return false;
	}
}
