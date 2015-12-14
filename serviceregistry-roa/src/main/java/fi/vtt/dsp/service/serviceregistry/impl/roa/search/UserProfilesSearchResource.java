package fi.vtt.dsp.service.serviceregistry.impl.roa.search;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBUserProfileDAO;
import fi.vtt.dsp.service.serviceregistry.common.UserProfile;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceDiscoveryException;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemCollectionResource;

/**
 * Main container resource for maintaining and managing user profiles
 * 
 * @author Antti Nummiaho
 * @version 1.0
 * @created 1-huhti-2014 14:43:34
 */
@Path("/resourcedirectory/v1/users/search")
public class UserProfilesSearchResource
		extends
			ServiceRegistryItemCollectionResource<UserProfile> {

	private static final Logger LOGGER = Logger
			.getLogger(UserProfilesSearchResource.class.getName());

	public UserProfilesSearchResource() {
		super(UserProfile.class);
	}

	/**
	 * Returns a list of all user profiles matching search context
	 */
	@GET
	@Produces("application/json")
	public Response searchListOfUserProfiles(@Context UriInfo uriInfo,
			@Context SearchContext searchContext) {
		Response resp = null;
		SearchCondition<UserProfile> conditionUP = null;
		try {
			List<UserProfile> userProfiles = null;
			MongoDBUserProfileDAO userProfileDAO = new MongoDBUserProfileDAO();
			if (searchContext != null) {
				conditionUP = searchContext.getCondition(UserProfile.class);
			}
			if (conditionUP != null) {
				userProfiles = userProfileDAO.getAll();
				userProfiles = conditionUP.findAll(userProfiles);
			}
			resp = Response.ok(
					userProfiles.toArray(new UserProfile[userProfiles.size()]))
					.build();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.toString());
			ServiceDiscoveryException ex = new ServiceDiscoveryException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("9005");
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
