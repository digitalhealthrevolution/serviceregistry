package fi.vtt.dsp.service.serviceregistry.auth.resource;

import fi.vtt.dsp.service.serviceregistry.auth.ResourceCollectionAuthorization;
import fi.vtt.dsp.service.serviceregistry.auth.token.Agent;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class UserProfilesResourceAuthorization
		extends
			ResourceCollectionAuthorization {
    private static final Logger LOGGER = Logger.getLogger(UserProfilesResourceAuthorization.class.getName());

	public UserProfilesResourceAuthorization() throws DAOGeneralSystemFault {
		super();
	}
   
    public boolean canList(Authentication auth) {
        if (isResourceNotFound()) {
            return true;
        }

		return true;
    }
}
