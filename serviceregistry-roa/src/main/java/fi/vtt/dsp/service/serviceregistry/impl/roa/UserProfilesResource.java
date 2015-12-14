package fi.vtt.dsp.service.serviceregistry.impl.roa;

import fi.vtt.dsp.service.serviceregistry.auth.token.Agent;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.springframework.security.access.prepost.PreAuthorize;

import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBUserProfileDAO;
import fi.vtt.dsp.service.serviceregistry.common.UserProfile;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationUserInvalidException;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemCollectionResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Main container resource for maintaining and managing user profiles
 * 
 * @author Antti Nummiaho
 * @version 1.0
 * @created 1-huhti-2014 14:43:34
 */
@Path("/resourcedirectory/v1/users")
public class UserProfilesResource
		extends
			ServiceRegistryItemCollectionResource<UserProfile> {

	private static final Logger LOGGER = Logger
			.getLogger(UserProfilesResource.class.getName());

	private Response resp = null;

	public UserProfilesResource() {
		super(UserProfile.class);
	}

	/**
	 * Returns a list of all user profiles
	 */
	@GET
	@Produces("application/json")
        @PreAuthorize("@userProfilesAuth.canList(authentication)")
	public Response getListOfUserProfiles(@Context UriInfo uriInfo,
			@Context SearchContext searchContext) {
		try {
                    Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
                    
		String userId = null;

		if (auth != null) {
			try {
				Agent agent = (Agent) auth.getDetails();
				userId = agent.getId();
			} catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error while getting list of user-profiles", e);
			}
		}
                
			MongoDBUserProfileDAO userProfileDAO = new MongoDBUserProfileDAO();
			List<UserProfile> userProfiles = userProfileDAO.getAll();
			
			for (UserProfile userProfile : userProfiles) {
				userProfile.setEmail("");
			}
			
			resp = Response.ok(
					userProfiles.toArray(new UserProfile[userProfiles.size()]))
					.build();
		} catch (Exception e) {
			ServiceRegistrationUserInvalidException ex = new ServiceRegistrationUserInvalidException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("8004");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	/**
	 * Creates and adds a new user profile. Returns URI to the created user
	 * profile.
	 * 
	 * @param UserProfile
	 *            Representation of the user profile that is to be added
	 */
	@POST
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@userProfilesAuth.canCreate(authentication)")
	public Response createUserProfile(@Context UriInfo uriInfo,
			UserProfile userProfile) {
		resp = null;
		try {
			MongoDBUserProfileDAO userProfileDAO = new MongoDBUserProfileDAO();
			String id = userProfileDAO.create(userProfile);
			userProfile.setUserId(id);

			if (id == null) {
				resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.build();
			} else {
				UriBuilder createdServiceURI = uriInfo.getAbsolutePathBuilder();
				createdServiceURI.path(id);
				resp = Response.created(createdServiceURI.build()).build();
			}
		}

		catch (Exception e) {
			ServiceRegistrationUserInvalidException ex = new ServiceRegistrationUserInvalidException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("8005");
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
