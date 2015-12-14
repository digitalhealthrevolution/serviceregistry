package fi.vtt.dsp.service.serviceregistry.auth.resource;

import fi.vtt.dsp.service.serviceregistry.auth.ResourceAuthorization;
import fi.vtt.dsp.service.serviceregistry.auth.token.Agent;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.common.GroupRole;
import fi.vtt.dsp.service.serviceregistry.common.UserGroup;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBUserGroupDAO;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.security.core.Authentication;

public class BindingResourceAuthorization extends ResourceAuthorization {
	private static final Logger LOGGER = Logger
		.getLogger(BindingResourceAuthorization.class.getName());

	public BindingResourceAuthorization() throws DAOGeneralSystemFault {
		super();
	}

	public BindingResourceAuthorization set(String serviceId,
		String serviceInstanceId, String bindingId)
		throws DAOGeneralSystemFault {
		service = this.getExistingService(serviceId);
		instance = this
			.getExistingServiceInstance(serviceId, serviceInstanceId);
		binding = this.getExistingBinding(serviceId, serviceInstanceId,
			bindingId);
		return this;
	}

	protected boolean authorizeRegisteredUserModify(String userId) {
		// enable modify access if the user owns the instance, or binding is
		// requested by the user
		return isUserAuthorizedToModifyService(userId, service)
			|| userId.equals(binding.getRequestedByUserId());
	}

	protected boolean authorizeRegisteredServiceModify(String serviceId) {
		// enable modify access if the instance is instance of the service, or
		// if the binding is for the service
		return this.isSame(service, serviceId)
			|| serviceId.equals(binding.getBoundByServiceId());
	}

	protected boolean authorizeRegisteredServiceInstanceModify(
		String serviceInstanceId) {
		// enable modify access if the instance is trying to modify itself, or
		// if the binding is for the instance
		return this.isSame(instance, serviceInstanceId)
			|| serviceInstanceId.equals(binding
				.getBoundByServiceInstanceId());
	}

	public boolean canGet(Authentication authentication) {
		if (isResourceNotFound()) {
			return true;
		}

		LOGGER.log(Level.FINE, "canGet called");
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
				LOGGER.log(Level.FINE, "is USER");
				boolean authorized = isUserAuthorizedToAccessInstance(agentId);
				LOGGER.log(Level.FINE, "authorized? " + authorized);
				return authorized
					|| instance.getServiceInstanceId().equals(
						binding.getBoundByServiceInstanceId());
			}

		}

		return false;
	}

	@Override
	public boolean canDelete(Authentication auth) throws DAOGeneralSystemFault,
		DAONotFoundFault {
		LOGGER.log(Level.FINE, "canDelete called");
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
				if (service.getServiceId().equals(agentId)|| agentId.equals(binding.getBoundByServiceId())) {
					return true;
				}
			}
			else if (agent.getType() == AgentType.SERVICE_INSTANCE) {
				if (instance.getServiceInstanceId().equals(agentId) || agentId.equals(binding.getBoundByServiceInstanceId())) {
					return true;
				}
			}			
			else if (agent.getType() == AgentType.USER) {
				LOGGER.log(Level.FINE, "is USER");
				boolean authorized = isUserAuthorizedToModifyService(agentId,
					service)
					|| agentId.equals(binding.getRequestedByUserId());
				LOGGER.log(Level.FINE, "authorized? " + authorized);
				return authorized;
			}

		}

		return false;
	}

	@Override
	public boolean canUpdate(Authentication authentication) {
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
					return true;
				}
			}

			if (agent.getType() == AgentType.SERVICE_INSTANCE) {
				if (instance.getServiceInstanceId().equals(agentId)) {
					return true;
				}
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
	
	public boolean canGetKey(Authentication authentication) {
		Agent agent = getAgent(authentication);
		String agentId = agent.getId();
		
		if (agent.getRole() == AgentRole.ROLE_ADMIN) {
			return true;
		}
		else if (agent.getRole() == AgentRole.ROLE_GUEST) {
			return false;
		}		
		else if (agent.getRole() == AgentRole.ROLE_REGISTERED) {
			if (agent.getType() == AgentType.SERVICE) {
				if (service.getServiceId().equals(agentId)) {
					return true;
				}
				else if (binding.getBoundByServiceId().equals(agentId) && binding.isStatusAuthorized()) {
					return true;
				}
			}
			else if (agent.getType() == AgentType.SERVICE_INSTANCE) {
				if (instance.getServiceInstanceId().equals(agentId)) {
					return true;
				}
				else if (binding.getBoundByServiceInstanceId().equals(agentId) && binding.isStatusAuthorized()) {
					return true;
				}
			}
			else if (agent.getType() == AgentType.USER) {
				if (service.getServiceDescription().getCreatedByUserId().equals(agentId)) {
					return true;
				}
				else if (binding.getRequestedByUserId().equals(agentId) && binding.isStatusAuthorized()) {
					return true;
				}
				
				try {
					MongoDBUserGroupDAO userGroupDAO = new MongoDBUserGroupDAO();
					List<UserGroup> userGroups = null;

					userGroups = userGroupDAO.getAllUserGroupsForUser(agentId);

					for (UserGroup userGroup : userGroups) {
						for (String serviceId : userGroup.getServiceRegistryEntryId()) {
							if (service.getServiceId().equals(serviceId)) {
								for (GroupRole groupRole : userGroup.getGroupRole()) {
									if (groupRole.getUserId().equals(agentId)) {
										return true;
									}
								}
							}
						}
					}
				}
				catch (Exception e) {
					LOGGER.log(Level.SEVERE, "Error checking access right for canGetKey", e);
					return false;
				}	
			}
		}
		
		return false;
	}
}
