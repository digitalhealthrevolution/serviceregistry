package fi.vtt.dsp.service.serviceregistry.impl.roa;

import fi.vtt.dsp.service.serviceregistry.auth.token.Agent;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

import org.springframework.security.access.prepost.PreAuthorize;

import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBUserProfileDAO;
import fi.vtt.dsp.service.serviceregistry.common.UserProfile;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationUserInvalidException;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemResource;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Individual resource for maintaining individual user profile
 * 
 * @author Antti Nummiaho
 * @version 1.0
 * @created 1-huhti-2014 14:43:34
 */
@Path("/resourcedirectory/v1/users/{userprofileid}")
public class UserProfileResource
		extends
			ServiceRegistryItemResource<UserProfile> {
    private static final Logger LOGGER = Logger.getLogger(UserProfileResource.class.getName());

	private Response resp = null;

	public UserProfileResource() {
		super(UserProfile.class);
	}

	/**
	 * Returns user profile
	 */
	@GET
	@Produces("application/json")
        @PreAuthorize("@userProfileAuth.set(#userProfileId).canGet(authentication)")
	public Response getUserProfileUserId(
			@PathParam("userprofileid") String userProfileId) {
		resp = null;
		boolean showEmail = false;
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String userId = null;

		if (auth != null) {
			try {
				Agent agent = (Agent) auth.getDetails();
				userId = agent.getId();
				
				if (userProfileId.equals(userId)) {
					showEmail = true;
				}
			} 
			catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error getting user-profile", e);
			}
		}
		
		try {
			MongoDBUserProfileDAO userProfileDAO = new MongoDBUserProfileDAO();
			UserProfile userProfile = userProfileDAO
					.findUserProfileByUserId(userProfileId);
			if (userProfile == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			
			if (!showEmail) {
				userProfile.setEmail("");
			}
			
			resp = Response.ok(userProfile).build();
		} catch (Exception e) {
			ServiceRegistrationUserInvalidException ex = new ServiceRegistrationUserInvalidException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("8001");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}
	/**
	 * Updates the user profile
	 * 
	 * @param updatedUserProfile
	 *            Updated user profile
	 */
	@PUT
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@userProfileAuth.set(#userProfileId).canUpdate(authentication)")
	public Response updateUserProfile(
			@PathParam("userprofileid") String userProfileId,
			UserProfile userProfile) {
		resp = null;
		try {
			// validates user profile
			if (userProfile.getUserId() == null) {
				userProfile.setUserId(userProfileId);
			}	

			MongoDBUserProfileDAO userProfileDAO = new MongoDBUserProfileDAO();
			if (userProfileDAO.update(userProfileId, userProfile)) {
				resp = Response.ok().build();
			} else {
				resp = Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			ServiceRegistrationUserInvalidException ex = new ServiceRegistrationUserInvalidException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("8002");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	/**
	 * Removes the user profile
	 */

	@DELETE
	@PreAuthorize("@userProfileAuth.set(#userProfileId).canDelete(authentication)")
	public Response deleteUserProfile(
			@PathParam("userprofileid") String userProfileId) {
		resp = null;
		try {
			MongoDBUserProfileDAO userProfileDAO = new MongoDBUserProfileDAO();
            
			if (userProfileDAO.delete(userProfileId)) {
				resp = Response.ok().build();
			} else {
				resp = Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			ServiceRegistrationUserInvalidException ex = new ServiceRegistrationUserInvalidException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("8003");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	/*
	 * Explicit OPTIONS method related to that javascript cross-domain BS
	 */
	@OPTIONS
	public Response returnOptions() {
		return Response.status(Response.Status.NO_CONTENT).build();
	}

}