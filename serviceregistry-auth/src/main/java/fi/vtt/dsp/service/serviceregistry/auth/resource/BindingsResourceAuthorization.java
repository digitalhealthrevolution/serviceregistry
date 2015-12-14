package fi.vtt.dsp.service.serviceregistry.auth.resource;

import fi.vtt.dsp.service.serviceregistry.auth.ResourceCollectionAuthorization;
import fi.vtt.dsp.service.serviceregistry.auth.token.Agent;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.common.GroupRole;
import fi.vtt.dsp.service.serviceregistry.common.UserGroup;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBUserGroupDAO;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.security.core.Authentication;

public class BindingsResourceAuthorization
	extends
	ResourceCollectionAuthorization {
	private static final Logger LOGGER = Logger.getLogger(BindingsResourceAuthorization.class.getName());

	public BindingsResourceAuthorization() throws DAOGeneralSystemFault {
		super();
	}

	public BindingsResourceAuthorization set(String serviceId,
		String serviceInstanceId) throws DAOGeneralSystemFault {
		this.service = this.getExistingService(serviceId);
		this.instance = this.getExistingServiceInstance(serviceId,
			serviceInstanceId);
		return this;
	}

	protected boolean authorizeRegisteredUserCreate(String userId) {
		return true;
	}

	protected boolean authorizeRegisteredServiceCreate(String serviceId) {
		return !this.isSame(service, serviceId);
	}

	protected boolean authorizeRegisteredServiceInstanceCreate(
		String serviceInstanceId) {
		return !this.isSame(instance, serviceInstanceId);
	}

	public boolean canList(Authentication authentication) {
		if (isResourceNotFound()) {
			return true;
		}

		Agent agent = getAgent(authentication);
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
			}
			else if (agent.getType() == AgentType.SERVICE_INSTANCE) {
				if (instance.getServiceInstanceId().equals(agentId)) {
					return true;
				}

				return false;
			}
			else if (agent.getType() == AgentType.UNDEFINED) {
				return false;
			}
			else if (agent.getType() == AgentType.USER) {
				return isUserAuthorizedToAccessInstance(agentId);
			}
		}

		return false;
	}
	
	@Override
	public boolean canCreate(Authentication authentication) {
		Agent agent = getAgent(authentication);
		String agentId = agent.getId();
		String servicesOwnerGroup = service.getServiceDescription().getOwnerGroup();

		if (agent.getRole() == AgentRole.ROLE_ADMIN) {
			return true;
		}

		if (agent.getRole() == AgentRole.ROLE_GUEST) {
			return false;
		}

		if (agent.getRole() == AgentRole.ROLE_REGISTERED) {
			if (agent.getType() == AgentType.SERVICE) {
				if (service.getServiceId().equals(agentId)) {
					return false;
				}
			}
			else if (agent.getType() == AgentType.SERVICE_INSTANCE) {
				if (instance.getServiceInstanceId().equals(agentId)) {
					return false;
				}
			}
			
			if (servicesOwnerGroup.equals("public")) {
				return true;
			}
			
			if (agent.getType() == AgentType.USER) {
				if (servicesOwnerGroup.equals("group")) {
					try {
						MongoDBUserGroupDAO userGroupDAO = new MongoDBUserGroupDAO();
						List<UserGroup> userGroups = null;

						userGroups = userGroupDAO.getAllUserGroupsForUser(agentId);

						for (UserGroup userGroup : userGroups) {
							for (String serviceId : userGroup.getServiceRegistryEntryId()) {
								if (service.getServiceId().equals(serviceId)) {
									for (GroupRole groupRole : userGroup.getGroupRole()) {
										if (groupRole.getUserId().equals(agentId)) {
											if (groupRole.getAccessRights().equals("write")) {
												return true;
											}
										}
									}
								}
							}
						}
					}
					catch (Exception e) {
						LOGGER.log(Level.SEVERE, "Error checking access right for canCreate", e);
						return false;
					}
				}				
			}
		}

		return false;
	}
	
	// Note. Used only in outgoing-bindings get
	public boolean canGet(Authentication authentication) {
		Agent agent = getAgent(authentication);
		String agentId = agent.getId();
		String servicesOwnerGroup = service.getServiceDescription().getOwnerGroup();

		if (agent.getRole() == AgentRole.ROLE_ADMIN) {
			return true;
		}

		if (agent.getRole() == AgentRole.ROLE_GUEST) {
			return true;
		}

		if (agent.getRole() == AgentRole.ROLE_REGISTERED) {
			if (agent.getType() == AgentType.SERVICE) {
				if (service.getServiceId().equals(agentId)) {
					return true;
				}
			}
			else if (agent.getType() == AgentType.SERVICE_INSTANCE) {
				if (instance.getServiceInstanceId().equals(agentId)) {
					return true;
				}
			}
			
			if (servicesOwnerGroup.equals("public")) {
				return true;
			}
			
			if (agent.getType() == AgentType.USER) {
				if (service.getServiceDescription().getCreatedByUserId().equals(agentId)) {
					return true;
				}
				
				if (servicesOwnerGroup.equals("group")) {
					try {
						MongoDBUserGroupDAO userGroupDAO = new MongoDBUserGroupDAO();
						List<UserGroup> userGroups = null;

						userGroups = userGroupDAO.getAllUserGroupsForUser(agentId);

						for (UserGroup userGroup : userGroups) {
							for (String serviceId : userGroup.getServiceRegistryEntryId()) {
								if (service.getServiceId().equals(serviceId)) {
									for (GroupRole groupRole : userGroup.getGroupRole()) {
										if (groupRole.getUserId().equals(agentId)) {
											if (groupRole.getAccessRights().equals("write")) {
												return true;
											}
										}
									}
								}
							}
						}
					}
					catch (Exception e) {
						LOGGER.log(Level.SEVERE, "Error checking access right for canCreate", e);
						return false;
					}
				}				
			}
		}

		return false;
	}
}
