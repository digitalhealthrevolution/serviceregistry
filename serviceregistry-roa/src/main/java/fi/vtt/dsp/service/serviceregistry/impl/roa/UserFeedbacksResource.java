package fi.vtt.dsp.service.serviceregistry.impl.roa;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.springframework.security.access.prepost.PreAuthorize;

import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationDoesNotExistException;
import fi.vtt.dsp.service.serviceregistry.common.description.UserFeedback;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemCollectionResource;
import fi.vtt.dsp.serviceframework.exceptions.ServerErrorException;
import java.util.logging.Logger;

/**
 * Main container resource for maintaining and managing user feedback in the
 * given service description
 * 
 * @author ELETAI
 * @version 1.0
 * @created 31-tammi-2014 14:43:34
 */
// Feedbacks is crappy English but for the sake of consistency
@Path("resourcedirectory/v1/serviceregistrations/{serviceid}/servicedescription/userfeedbacks")
public class UserFeedbacksResource
		extends
			ServiceRegistryItemCollectionResource<UserFeedback> {

	private static final Logger LOGGER = Logger
			.getLogger(UserFeedbacksResource.class.getName());
		
	public UserFeedbacksResource() {
		super(UserFeedback.class);
	}

	/**
	 * Creates and adds a new item into the collection. Returns URI to the
	 * created item resources.
	 * 
	 * @param UserFeedback
	 *            Representation of the resource to be added into the collection
	 */
	@POST
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@userFeedbacksAuth.set(#serviceId).canCreate(authentication)")
	public Response createUserFeedback(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId, UserFeedback userFeedback) {
		// TODO: HATEOAS linking, see ItemCollectionResource
		Response resp = null;
		String newUserFeebackId;
		try {
			newUserFeebackId = this.createItem(serviceId, userFeedback);
			UriBuilder createdServiceURI = uriInfo.getAbsolutePathBuilder();
			createdServiceURI.path(newUserFeebackId);
			resp = Response.created(createdServiceURI.build()).build();
		} catch (Exception e) {			
			ServerErrorException ex = new ServerErrorException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("6004");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	/**
	 * Reads and returns a list of the contained items
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canGet(authentication)")
	public Response getListOfUserFeedback(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId) {
		// TODO: HATEOAS linking, see ItemCollectionResource
		Response resp = null;
		try {
			MultivaluedMap<String, String> queryParams = uriInfo
					.getQueryParameters();
			List<UserFeedback> userFBs = this.readListOfItems(serviceId, null,
					queryParams.entrySet(), uriInfo);
			resp = Response.ok(
					userFBs.toArray(new UserFeedback[userFBs.size()])).build();

		} catch (Exception e) {		
			if (e instanceof DAONotFoundFault) {
				ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
				ex.setExceptionReason(e.getMessage());
				resp = this.convertExceptionToResponse(ex, e);
			} else {
				ServerErrorException ex = new ServerErrorException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("6005");
				resp = this.convertExceptionToResponse(ex, e);
			}
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