package fi.vtt.dsp.service.serviceregistry.impl.roa;

import fi.vtt.dsp.service.serviceregistry.common.UserGroup;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationDoesNotExistException;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBUserGroupDAO;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemCollectionResource;
import fi.vtt.dsp.serviceframework.exceptions.ServerErrorException;
import java.util.List;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.springframework.security.access.prepost.PreAuthorize;

@Path("/resourcedirectory/v1/usergroups")
public class UserGroupsResource
		extends
			ServiceRegistryItemCollectionResource<UserGroup> {
	private static final Logger LOGGER = Logger
			.getLogger(UserProfilesResource.class.getName());

	public UserGroupsResource() {
		super(UserGroup.class);
	}

	/**
	 * Returns a list of all user groups
	 */
	@GET
	@Produces("application/json")
	public Response getListOfUserGroups(@Context UriInfo uriInfo,
			@Context SearchContext searchContext) {
		Response response = null;

		try {
			MongoDBUserGroupDAO userGroupDAO = new MongoDBUserGroupDAO();
			List<UserGroup> userGroups = userGroupDAO.getAllUserGroups();
			response = Response.ok(
					userGroups.toArray(new UserGroup[userGroups.size()]))
					.build();
		} catch (Exception e) {
			if (e instanceof DAONotFoundFault) {
				ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("7003");
				response = this.convertExceptionToResponse(ex, e);
			} else {
				ServerErrorException ex = new ServerErrorException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("7003");
				response = this.convertExceptionToResponse(ex, e);
			}
		}

		return response;
	}

	/**
	 * Creates and adds a new user group. Returns URI to the created user group.
	 * 
	 * @param UserGroup
	 *            Representation of the user group that is to be added
	 */
	@POST
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@userGroupsAuth.canCreate(authentication)")
	public Response createUserGroup(@Context UriInfo uriInfo,
			UserGroup userGroup) {
		Response response = null;

		try {
			MongoDBUserGroupDAO userGroupDAO = new MongoDBUserGroupDAO();
			String id = userGroupDAO.insertNewUserGroup(userGroup);
			userGroup.setUserGroupId(id);

			if (id == null) {
				response = Response.status(
						Response.Status.INTERNAL_SERVER_ERROR).build();
			} else {
				UriBuilder createdServiceURI = uriInfo.getAbsolutePathBuilder();
				createdServiceURI.path(id);
				response = Response.created(createdServiceURI.build()).build();
			}
		}

		catch (Exception e) {
			ServerErrorException ex = new ServerErrorException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("7004");
			response = this.convertExceptionToResponse(ex, e);
        }

		return response;
	}

	/*
	 * Explicit OPTIONS method related to that javascript cross-domain BS
	 */
	@OPTIONS
	public Response returnOptions() {
		return Response.status(Response.Status.NO_CONTENT).build();
	}
}
