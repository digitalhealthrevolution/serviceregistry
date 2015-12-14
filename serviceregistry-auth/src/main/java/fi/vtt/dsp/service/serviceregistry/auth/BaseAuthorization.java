package fi.vtt.dsp.service.serviceregistry.auth;

import org.springframework.security.core.Authentication;

import fi.vtt.dsp.service.serviceregistry.auth.token.Agent;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.common.GroupRole;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.UserProfile;
import fi.vtt.dsp.service.serviceregistry.common.UserGroup;
import fi.vtt.dsp.service.serviceregistry.common.description.ServiceDescription;
import fi.vtt.dsp.service.serviceregistry.common.description.UserFeedback;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceInstanceDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceRegistryDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBUserGroupDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBUserProfileDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.ServiceInstanceDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.ServiceRegistryDAO;
import fi.vtt.dsp.serviceframework.common.Binding;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.security.core.context.SecurityContextHolder;

@SuppressWarnings("unused")
public abstract class BaseAuthorization {
	private static final Logger LOGGER = Logger
			.getLogger(BaseAuthorization.class.getName());
	private static final boolean DEFAULT_AUTH_ADMIN_LIST = true;
	private static final boolean DEFAULT_AUTH_REGISTERED_LIST = true;
	private static final boolean DEFAULT_AUTH_GUEST_LIST = true;

	final private ServiceRegistryDAO serviceRegistryDAO;
	final private ServiceInstanceDAO serviceInstanceDAO;
	final private MongoDBUserProfileDAO userProfileDAO;
	final private MongoDBUserGroupDAO mongoDBUserGroupDAO;

	private boolean resourceNotFound = false;

	protected ServiceRegistryEntry service;
	protected ServiceInstance instance;
	protected Binding binding;
	protected UserFeedback userFeedback;
	protected UserGroup userGroup;
	protected UserProfile profile;

	public BaseAuthorization() throws DAOGeneralSystemFault {
		serviceRegistryDAO = new MongoDBServiceRegistryDAO();
		serviceInstanceDAO = new MongoDBServiceInstanceDAO();
		userProfileDAO = new MongoDBUserProfileDAO();
		mongoDBUserGroupDAO = new MongoDBUserGroupDAO();
	}

	protected boolean isResourceNotFound() {
		return resourceNotFound;
	}

    protected Agent getAgent(Authentication auth) {
        Object details = auth.getDetails();
        Agent agent;
    
        if (details instanceof Agent) {
            agent = (Agent) details;
        } else {
            agent = new Agent("", AgentType.UNDEFINED, AgentRole.ROLE_GUEST);
        }
        
        return agent;
    }

	protected ServiceRegistryEntry getExistingService(String serviceId)
			throws DAOGeneralSystemFault {
		ServiceRegistryEntry service = null;
		try {
			service = serviceRegistryDAO.findServiceRegistryEntry(serviceId);
			resourceNotFound = false;
		} catch (DAONotFoundFault e) {
			resourceNotFound = true;
		}
		return service;
	}

	protected UserProfile getExistingProfile(String userId)
			throws DAOGeneralSystemFault {
		UserProfile prof = null;
		try {
			prof = userProfileDAO.findUserProfileByUserId(userId);
			resourceNotFound = false;
		} catch (DAONotFoundFault e) {
			resourceNotFound = true;
		}
		return prof;
	}

	protected UserGroup getExistingUserGroup(String groupId)
			throws DAOGeneralSystemFault {
		UserGroup group = null;

		try {
			group = mongoDBUserGroupDAO.getUserGroupById(groupId);
			resourceNotFound = false;
		} catch (DAONotFoundFault e) {
			resourceNotFound = true;
		}

		return group;
	}

	protected ServiceInstance getExistingServiceInstance(String serviceId,
			String serviceInstanceId) throws DAOGeneralSystemFault {
		ServiceInstance inst = null;
		try {
			inst = serviceInstanceDAO.findServiceInstance(serviceId,
					serviceInstanceId);
			resourceNotFound = false;
		} catch (DAONotFoundFault e) {
			resourceNotFound = true;
		}
		return inst;
	}

	protected UserFeedback getExistingUserFeedback(String serviceId,
			String userFeedbackId) throws DAOGeneralSystemFault {
		UserFeedback feedback = null;
		try {
			ServiceRegistryEntry service = serviceRegistryDAO
					.findServiceRegistryEntry(serviceId);
			resourceNotFound = false;
			ServiceDescription desc = service.getServiceDescription();
			if (desc != null) {
				for (UserFeedback fb : desc.getUserFeedback()) {
					if (fb.getUserFeedbackId().equals(userFeedbackId)) {
						feedback = fb;
						break;
					}
				}
			}
		} catch (DAONotFoundFault e) {
			resourceNotFound = true;
		}

		if (feedback == null) {
			resourceNotFound = true;
		}

		return feedback;
	}

	protected Binding getExistingBinding(String serviceId,
			String serviceInstanceId, String bindingId)
			throws DAOGeneralSystemFault {
		Binding binding = null;
		try {
			ServiceInstance inst = serviceInstanceDAO.findServiceInstance(
					serviceId, serviceInstanceId);
			resourceNotFound = false;
			ServiceAccessEndPoint ep = inst.getServiceAccessEndPoint();
			if (ep != null) {
				for (Binding b : ep.getBinding()) {
					if (b.getBindingId().equals(bindingId)) {
						binding = b;
						break;
					}
				}
			}
		} catch (DAONotFoundFault e) {
			resourceNotFound = true;
		}

		if (binding == null) {
			resourceNotFound = true;
		}

		return binding;
	}

	protected boolean isCreatedByUser(UserGroup userGroup, String userId) {
		String existingUserGroupCreator = userGroup.getCreatedByUserId();
		return existingUserGroupCreator.equals(userId);
	}

	protected boolean isCreatedByUser(ServiceRegistryEntry existingService,
			String userId) {
		String existingServiceCreator = existingService.getServiceDescription()
				.getCreatedByUserId();
		return existingServiceCreator.equals(userId);
	}

	protected boolean isCreatedByUser(ServiceInstance existingInstance,
			String userId) {
		String existingInstanceCreator = existingInstance.getCreatedByUserId();
		return existingInstanceCreator.equals(userId);
	}

	protected boolean isCreatedByUser(UserFeedback existingFb, String userId) {
		String existingFbCreator = existingFb.getProvidedByUserId();
		return existingFbCreator.equals(userId);
	}

	protected boolean isSame(UserProfile profile, String userId) {
		return profile.getUserId().equals(userId);
	}

	protected boolean isSame(ServiceRegistryEntry service, String serviceId) {
		return service.getServiceId().equals(serviceId);
	}

	protected boolean isSame(ServiceInstance instance, String serviceInstanceId) {
		return instance.getServiceInstanceId().equals(serviceInstanceId);
	}

	protected boolean isInstancesService(ServiceInstance instance,
			ServiceRegistryEntry service) {
		boolean exists = false;
		for (ServiceInstance inst : service.getServiceInstance()) {
			if (inst.getServiceInstanceId().equals(
					instance.getServiceInstanceId())) {
				exists = true;
				break;
			}
		}
		return exists;
	}

	protected boolean authorizeAdminUserList(String userId) {
		return DEFAULT_AUTH_ADMIN_LIST;
	}

	protected boolean authorizeAdminServiceList(String serviceId) {
		return DEFAULT_AUTH_ADMIN_LIST;
	}

	protected boolean authorizeAdminServiceInstanceList(String serviceInstanceId) {
		return DEFAULT_AUTH_ADMIN_LIST;
	}

	protected boolean authorizeRegisteredUserList(String userId) {
		return DEFAULT_AUTH_REGISTERED_LIST;
	}

	protected boolean authorizeRegisteredServiceList(String serviceId) {
		return DEFAULT_AUTH_REGISTERED_LIST;
	}

	protected boolean authorizeRegisteredServiceInstanceList(
			String serviceInstanceId) {
		return DEFAULT_AUTH_REGISTERED_LIST;
	}

	protected boolean authorizeGuestList() {
		return DEFAULT_AUTH_GUEST_LIST;
	}

	public boolean canList(Authentication auth) {
		Agent agent = getAgent(auth);

		String agentId = agent.getId();
		boolean authorized = false;

		switch (agent.getRole()) {
			case ROLE_ADMIN :
				switch (agent.getType()) {
					case USER :
						authorized = authorizeAdminUserList(agentId);
						break;
					case SERVICE :
						authorized = authorizeAdminServiceList(agentId);
						break;
					case SERVICE_INSTANCE :
						authorized = authorizeAdminServiceInstanceList(agentId);
						break;
					default :
						break;
				}
				break;
			case ROLE_REGISTERED :
				switch (agent.getType()) {
					case USER :
						authorized = authorizeRegisteredUserList(agentId);
						break;
					case SERVICE :
						authorized = authorizeRegisteredServiceList(agentId);
						break;
					case SERVICE_INSTANCE :
						authorized = authorizeRegisteredServiceInstanceList(agentId);
						break;
					default :
						break;
				}
				break;
			case ROLE_GUEST :
				authorized = authorizeGuestList();
				break;
			default :
				break;
		}

		return authorized;
	}

	public boolean isUserAuthorizedToAccessService(
			Authentication authentication, ServiceRegistryEntry service) {
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		String userId = null;

		if (auth != null) {
			try {
				Agent agent = (Agent) (Object) auth.getDetails();
				userId = agent.getId();

				if (agent.getRole() == AgentRole.ROLE_ADMIN) {
					return true;
				}
			} catch (Exception e) {
			}
		}

		return isUserAuthorizedToAccessService(userId, service);
	}

	public boolean isUserAuthorizedToModifyService(String userId,
			ServiceRegistryEntry service) {

		if (service.getServiceDescription().getCreatedByUserId().equals(userId)) {
			LOGGER.log(Level.FINE, "Service is owned by user, returning true");
			return true;
		}

		if (service.getServiceDescription().getOwnerGroup().equals("group")) {
			LOGGER.log(Level.FINE, "Service is group-owned");
			try {
				MongoDBUserGroupDAO userGroupDAO = new MongoDBUserGroupDAO();
				List<UserGroup> userGroups = null;

				userGroups = userGroupDAO.getAllUserGroupsForUser(userId);

				for (UserGroup userGroup : userGroups) {
					for (String serviceId : userGroup
							.getServiceRegistryEntryId()) {
						if (service.getServiceId().equals(serviceId)) {
							LOGGER.log(Level.FINE,
									"Service is group-owned, user if in group, returning true");

							for (GroupRole groupRole : userGroup.getGroupRole()) {
								if (groupRole.getUserId().equals(userId)) {
									if (groupRole.getAccessRights().equals(
											"write")) {
										return true;
									}
								}
							}

							return false;
						}
					}
				}

			} catch (Exception e) {
				LOGGER.log(Level.SEVERE,
						"Error checking access right for canGet", e);
				return false;
			}
		}

		LOGGER.log(Level.FINE,
				"User has no right to see the group, returning false");
		return false;
	}

	public boolean isUserAuthorizedToModifyInstance(String userId) {

		if (instance.getCreatedByUserId().equals(userId)) {
			LOGGER.log(Level.FINE, "Service is owned by user, returning true");
			return true;
		}

		if (service.getServiceDescription().getOwnerGroup().equals("group")) {
			LOGGER.log(Level.FINE, "Service is group-owned");
			try {
				MongoDBUserGroupDAO userGroupDAO = new MongoDBUserGroupDAO();
				List<UserGroup> userGroups = null;

				userGroups = userGroupDAO.getAllUserGroupsForUser(userId);

				for (UserGroup userGroup : userGroups) {
					for (String serviceId : userGroup
							.getServiceRegistryEntryId()) {
						if (service.getServiceId().equals(serviceId)) {
							LOGGER.log(Level.FINE,
									"Service is group-owned, user if in group, returning true");

							for (GroupRole groupRole : userGroup.getGroupRole()) {
								if (groupRole.getUserId().equals(userId)) {
									if (groupRole.getAccessRights().equals(
											"write")) {
										return true;
									}
								}
							}

							return false;
						}
					}
				}

			} catch (Exception e) {
				LOGGER.log(Level.SEVERE,
						"Error checking access right for canGet", e);
				return false;
			}
		}

		LOGGER.log(Level.FINE,
				"User has no right to see the group, returning false");
		return false;
	}

	public boolean isUserAuthorizedToAccessInstance(String userId) {

		if (instance.getCreatedByUserId().equals(userId)) {
			LOGGER.log(Level.FINE, "Service is owned by user, returning true");
			return true;
		}

		if (service.getServiceDescription().getOwnerGroup().equals("group")) {
			LOGGER.log(Level.FINE, "Service is group-owned");
			try {
				MongoDBUserGroupDAO userGroupDAO = new MongoDBUserGroupDAO();
				List<UserGroup> userGroups = null;

				userGroups = userGroupDAO.getAllUserGroupsForUser(userId);

				for (UserGroup userGroup : userGroups) {
					for (String serviceId : userGroup
							.getServiceRegistryEntryId()) {
						if (service.getServiceId().equals(serviceId)) {
							LOGGER.log(Level.FINE,
									"Service is group-owned, user if in group, returning true");

							for (GroupRole groupRole : userGroup.getGroupRole()) {
								if (groupRole.getUserId().equals(userId)) {
									return true;
								}
							}

							return false;
						}
					}
				}

			} catch (Exception e) {
				LOGGER.log(Level.SEVERE,
						"Error checking access right for canGet", e);
				return false;
			}
		}

		LOGGER.log(Level.FINE,
				"User has no right to see the group, returning false");
		return false;
	}

	public boolean isUserAuthorizedToAccessService(ServiceRegistryEntry service) {
		if (service.getServiceDescription().getOwnerGroup().equals("public")) {
			LOGGER.log(Level.FINE, "Service is public, returning true");
			return true;
		}
		
		return false;
	}
	
	public boolean isUserAuthorizedToAccessService(String userId,
			ServiceRegistryEntry service) {
		if (service.getServiceDescription().getCreatedByUserId().equals(userId)) {
			LOGGER.log(Level.FINE,
					"Service is private, user is owner, returning true");
			return true;
		}

		if (service.getServiceDescription().getOwnerGroup().equals("public")) {
			LOGGER.log(Level.FINE, "Service is public, returning true");
			return true;
		} else if (service.getServiceDescription().getOwnerGroup()
				.equals("private")) {
			if (userId == null) {
				LOGGER.log(Level.FINE, "Private, userId null, returning false");
				return false;
			}
		} else if (service.getServiceDescription().getOwnerGroup()
				.equals("group")) {
			LOGGER.log(Level.FINE, "Service is group-owned");
			try {
				MongoDBUserGroupDAO userGroupDAO = new MongoDBUserGroupDAO();
				List<UserGroup> userGroups = null;

				userGroups = userGroupDAO.getAllUserGroupsForUser(userId);

				for (UserGroup userGroup : userGroups) {
					for (String serviceId : userGroup
							.getServiceRegistryEntryId()) {
						if (service.getServiceId().equals(serviceId)) {
							LOGGER.log(Level.FINE,
									"Service is group-owned, user if in group, returning true");
							return true;
						}
					}
				}

			} catch (Exception e) {
				LOGGER.log(Level.SEVERE,
						"Error checking access right for canGet", e);
				return false;
			}
		}

		LOGGER.log(Level.FINE,
				"User has no right to see the group, returning false");
		return false;
	}
}
