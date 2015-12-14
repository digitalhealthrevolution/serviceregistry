package fi.vtt.dsp.service.serviceregistry.impl.roa;

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.UserGroup;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationUserInvalidException;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceRegistryDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBUserGroupDAO;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemResource;
import fi.vtt.dsp.serviceframework.exceptions.ServerErrorException;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.springframework.security.access.prepost.PreAuthorize;

@Path("/resourcedirectory/v1/usergroups/{usergroupid}")
public class UserGroupResource extends ServiceRegistryItemResource<UserGroup> {
	private static final Logger LOGGER = Logger.getLogger(UserGroupResource.class.getName());
	public UserGroupResource() {
		super(UserGroup.class);
	}

	@GET
	@Path("/servicesingroup")
	@Produces("application/json")
	@PreAuthorize("@userGroupAuth.set(#userGroupId).canGet(authentication)")
	public Response getListOfServicesInGroup(@PathParam("usergroupid") String userGroupId) {
		Response response = null;
		
		try {
			MongoDBServiceRegistryDAO mongoDBServiceRegistryDAO = new MongoDBServiceRegistryDAO();
			MongoDBUserGroupDAO mongoDBUserGroupDAO = new MongoDBUserGroupDAO();
		
			UserGroup userGroup = mongoDBUserGroupDAO.getUserGroupById(userGroupId);
			ArrayList<ServiceRegistryEntry> services = new ArrayList<>();
			
			for (String serviceId : userGroup.getServiceRegistryEntryId()) {
				ServiceRegistryEntry serviceRegistryEntry = mongoDBServiceRegistryDAO.findServiceRegistryEntry(serviceId);
				services.add(serviceRegistryEntry);
			}
			
			response = Response.ok(services.toArray(new ServiceRegistryEntry[services.size()])).build();
		} 
		catch (Exception e) {
			ServiceRegistrationUserInvalidException ex = new ServiceRegistrationUserInvalidException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("7001");
			response = this.convertExceptionToResponse(ex, e);
		}
		
		return response;
	}
	
	@GET
	@Produces("application/json")
	public Response readUserGroup(@Context UriInfo uriInfo,
			@PathParam("usergroupid") String userGroupId) {
		Response resp = null;

		try {
			MongoDBUserGroupDAO userGroupDAO = new MongoDBUserGroupDAO();
			UserGroup userGroup = userGroupDAO.getUserGroupById(userGroupId);

			if (userGroup == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}

			resp = Response.ok(userGroup).build();
		} catch (Exception e) {
			ServiceRegistrationUserInvalidException ex = new ServiceRegistrationUserInvalidException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("7001");
			resp = this.convertExceptionToResponse(ex, e);
		}

		return resp;
	}

	@PUT
    @Consumes("application/json")   
	@PreAuthorize("@userGroupAuth.set(#userGroupId).canUpdate(authentication)")
	public Response updateUserGroup(
			@PathParam("usergroupid") String userGroupId, UserGroup userGroup) {
		Response response = null;

		try {
			if (userGroup.getUserGroupId() == null) {
				userGroup.setUserGroupId(userGroupId);
			}

			MongoDBUserGroupDAO userGroupDAO = new MongoDBUserGroupDAO();

			if (userGroupDAO.updateUserGroup(userGroup)) {
				response = Response.ok().build();
			} else {
				response = Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			ServerErrorException ex = new ServerErrorException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("7002");
			response = this.convertExceptionToResponse(ex, e);
		}

		return response;
	}

	/**
	 * Removes the user profile
	 */
	@DELETE
	@PreAuthorize("@userGroupAuth.set(#userGroupId).canDelete(authentication)")
	public Response deleteUserGroup(@PathParam("usergroupid") String userGroup) {
		Response response = null;

		try {
			MongoDBUserGroupDAO userGroupDAO = new MongoDBUserGroupDAO();
			if (userGroupDAO.deleteUserGroup(userGroup)) {
				response = Response.ok().build();
			} else {
				response = Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			ServerErrorException ex = new ServerErrorException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("7002");
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
