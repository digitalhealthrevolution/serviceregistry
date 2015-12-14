package fi.vtt.dsp.service.serviceregistry.auth.resource;

import fi.vtt.dsp.service.serviceregistry.auth.ResourceAuthorization;
import fi.vtt.dsp.service.serviceregistry.auth.token.Agent;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import java.util.logging.Logger;
import org.springframework.security.core.Authentication;

public class UserFeedbackResourceAuthorization extends ResourceAuthorization {
	private static final Logger LOGGER = Logger
			.getLogger(UserFeedbackResourceAuthorization.class.getName());

	public UserFeedbackResourceAuthorization() throws DAOGeneralSystemFault {
		super();
	}

	public UserFeedbackResourceAuthorization set(String serviceId,
			String userFeedbackId) throws DAOGeneralSystemFault {
		service = getExistingService(serviceId);
		userFeedback = getExistingUserFeedback(serviceId, userFeedbackId);
		return this;
	}

	protected boolean authorizeRegisteredUserModify(String userId) {
		// return this.isCreatedByUser(userFeedback, userId);
		return isUserAuthorizedToModifyService(userId, service);
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
				return userFeedback.getProvidedByUserId().equals(agentId);
				// All user-feedback related stuff isn't implemented. This is
				// left for later use
				/*
				 * MongoDBUserGroupDAO userGroupDAO = new MongoDBUserGroupDAO();
				 * List<UserGroup> userGroups = null; userGroups =
				 * userGroupDAO.getAllUserGroupsForUser(agentId);
				 * 
				 * for (GroupRole groupRole : userGroup.getGroupRole()) { if
				 * (groupRole.getUserId().equals(agentId)) { if
				 * (groupRole.getAccessRights().equals("write")) { return true;
				 * } else { return false; } } }
				 */
			} else if (agent.getType() == AgentType.SERVICE) {
				return false; // All user-feedback related stuff isn't
								// implemented. Returning false for now
				/*
				 * if (service.getServiceId().equals(agentId)) { return true; }
				 * else { return false; }
				 */
			} else if (agent.getType() == AgentType.SERVICE_INSTANCE) {
				return false; // All user-feedback related stuff isn't
								// implemented. Returning false for now
				/*
				 * for (ServiceInstance serviceInstance :
				 * service.getServiceInstance()) { if
				 * (serviceInstance.getServiceInstanceId().equals(agentId)) {
				 * return true; } }
				 */
			}
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
				return userFeedback.getProvidedByUserId().equals(agentId);
				// To be dealt with later on
				/*
				 * for (GroupRole groupRole : userGroup.getGroupRole()) { if
				 * (groupRole.getUserId().equals(agentId)) { if
				 * (groupRole.getAccessRights().equals("write")) { return true;
				 * } else { return false; } } }
				 */
			} else if (agent.getType() == AgentType.SERVICE) {
				return false; // All user-feedback related stuff isn't
								// implemented. Returning false for now
				/*
				 * if (service.getServiceId().equals(agentId)) { return true; }
				 * else { return false; }
				 */
			} else if (agent.getType() == AgentType.SERVICE_INSTANCE) {
				return false; // All user-feedback related stuff isn't
								// implemented. Returning false for now
				/*
				 * for (ServiceInstance serviceInstance :
				 * service.getServiceInstance()) { if
				 * (serviceInstance.getServiceInstanceId().equals(agentId)) {
				 * return true; } }
				 */
			}
		}

		return false;
	}
}
