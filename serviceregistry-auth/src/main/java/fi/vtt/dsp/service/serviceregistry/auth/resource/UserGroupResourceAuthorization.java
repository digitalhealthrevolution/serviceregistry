package fi.vtt.dsp.service.serviceregistry.auth.resource;

import fi.vtt.dsp.service.serviceregistry.auth.ResourceAuthorization;
import fi.vtt.dsp.service.serviceregistry.auth.token.Agent;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.common.GroupRole;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import org.springframework.security.core.Authentication;

public class UserGroupResourceAuthorization extends ResourceAuthorization {
	public UserGroupResourceAuthorization() throws DAOGeneralSystemFault {
		super();
	}

	public UserGroupResourceAuthorization set(String userGroupId)
			throws DAOGeneralSystemFault {
		userGroup = this.getExistingUserGroup(userGroupId);
		return this;
	}

	protected boolean authorizeRegisteredUserModify(String userId) {
		return this.isCreatedByUser(userGroup, userId);
	}

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
			if (agent.getType() == AgentType.USER) {
				for (GroupRole groupRole : userGroup.getGroupRole()) {
					if (groupRole.getUserId().equals(agentId)) {
						if (groupRole.getAccessRights().equals("write")) {
							return true;
						} else {
							return false;
						}
					}
				}
			}

			return false;
		}

		if (agent.getType() == AgentType.SERVICE_INSTANCE) {
			return false;
		}

		if (agent.getType() == AgentType.UNDEFINED) {
			return false;
		}

		return false;
	}

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
			if (agent.getType() == AgentType.USER) {
				for (GroupRole groupRole : userGroup.getGroupRole()) {
					if (groupRole.getUserId().equals(agentId)) {
						if (groupRole.getAccessRights().equals("write")) {
							return true;
						} else {
							return false;
						}
					}
				}
			}

			return false;
		}

		if (agent.getType() == AgentType.SERVICE_INSTANCE) {
			return false;
		}

		if (agent.getType() == AgentType.UNDEFINED) {
			return false;
		}

		return false;
	}
	
	public boolean canGet(Authentication auth) throws DAOGeneralSystemFault, DAONotFoundFault {
		Agent agent = getAgent(auth);
		String agentId = agent.getId();

		if (agent.getRole() == AgentRole.ROLE_ADMIN) {
			return true;
		}

		if (agent.getRole() == AgentRole.ROLE_GUEST) {
			return false;
		}

		if (agent.getRole() == AgentRole.ROLE_REGISTERED) {
			if (agent.getType() == AgentType.USER) {
				for (GroupRole groupRole : userGroup.getGroupRole()) {
					if (groupRole.getUserId().equals(agentId)) {
						return true;
					}
				}
			}

			return false;
		}

		if (agent.getType() == AgentType.SERVICE_INSTANCE) {
			return false;
		}

		if (agent.getType() == AgentType.UNDEFINED) {
			return false;
		}

		return false;
	} 
}
