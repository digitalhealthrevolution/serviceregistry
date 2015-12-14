package fi.vtt.dsp.service.serviceregistry.impl.roa;

import fi.vtt.dsp.service.serviceregistry.common.GroupRole;
import fi.vtt.dsp.service.serviceregistry.common.UserGroup;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBUserGroupDAO;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemCollectionResource;
import fi.vtt.dsp.serviceframework.exceptions.ServerErrorException;
import java.util.List;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.springframework.security.access.prepost.PreAuthorize;

@Path("/resourcedirectory/v1/usergroups/user/{userprofileid}")
public class GroupRolesResource
		extends
			ServiceRegistryItemCollectionResource<GroupRole> {
	private static final Logger LOGGER = Logger
			.getLogger(GroupRolesResource.class.getName());

	public GroupRolesResource() {
		super(GroupRole.class);
	}

	/**
	 * Get list of user-groups for certain user
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@groupRolesAuth.set(#userProfileId).canGet(authentication)")
	public Response getListOfUserGroupsForUser(
			@PathParam("userprofileid") String userProfileId) {
		Response response = null;

		try {
			MongoDBUserGroupDAO userGroupDAO = new MongoDBUserGroupDAO();
			List<UserGroup> userGroups = userGroupDAO
					.getAllUserGroupsForUser(userProfileId);
			response = Response.ok(
					userGroups.toArray(new UserGroup[userGroups.size()]))
					.build();
		} catch (Exception e) {
			ServerErrorException ex = new ServerErrorException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("7005");
			response = this.convertExceptionToResponse(ex, e);
		}

		return response;
	}
}
