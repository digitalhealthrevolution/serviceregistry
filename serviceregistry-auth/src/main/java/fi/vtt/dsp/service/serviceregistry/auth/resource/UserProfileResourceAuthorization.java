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
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class UserProfileResourceAuthorization extends ResourceAuthorization {
    private static final Logger LOGGER = Logger.getLogger(UserProfileResourceAuthorization.class.getName());
    
	public UserProfileResourceAuthorization() throws DAOGeneralSystemFault {
		super();
	}

	public UserProfileResourceAuthorization set(String userProfileId)
			throws DAOGeneralSystemFault {
		profile = this.getExistingProfile(userProfileId);
		return this;
	}

	protected boolean authorizeRegisteredUserModify(String userId) {
		return this.isSame(profile, userId);
	}

        public boolean canGet(Authentication auth) throws DAOGeneralSystemFault, DAONotFoundFault {
            if (isResourceNotFound()) {
                return true;
            }

			return true;
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

		if (agent.getRole() == AgentRole.ROLE_REGISTERED) {
			if (agent.getType() == AgentType.USER) {
				if (profile.getUserId().equals(agentId)) {
					return true;
				}
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

		if (agent.getRole() == AgentRole.ROLE_REGISTERED) {
			if (agent.getType() == AgentType.USER) {
				if (profile.getUserId().equals(agentId)) {
					return true;
				}
			}
		}

		return false;
	}
}
