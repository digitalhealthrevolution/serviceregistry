package fi.vtt.dsp.service.serviceregistry.auth.resource;

import fi.vtt.dsp.service.serviceregistry.auth.ResourceAuthorization;
import fi.vtt.dsp.service.serviceregistry.auth.token.Agent;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import java.util.logging.Logger;
import org.springframework.security.core.Authentication;

public class GroupRolesResourceAuthorization extends ResourceAuthorization {
	private static final Logger LOGGER = Logger
			.getLogger(GroupRolesResourceAuthorization.class.getName());
	public GroupRolesResourceAuthorization() throws DAOGeneralSystemFault {
		super();
	}

	public GroupRolesResourceAuthorization set(String profileId)
			throws DAOGeneralSystemFault {
		profile = this.getExistingProfile(profileId);
		return this;
	}

	public boolean canGet(Authentication authentication) {
		if (isResourceNotFound()) {
			return true;
		}

		Agent agent = getAgent(authentication);

		if (agent.getRole() == AgentRole.ROLE_ADMIN) {
			return true;
		}

		if (agent.getRole() == AgentRole.ROLE_GUEST) {
			return true;
		}

		if (agent.getRole() == AgentRole.ROLE_REGISTERED) {
			return true;			
		}

		return true;
	}
}
