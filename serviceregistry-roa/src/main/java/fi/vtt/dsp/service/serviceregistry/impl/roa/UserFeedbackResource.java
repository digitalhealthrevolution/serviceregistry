package fi.vtt.dsp.service.serviceregistry.impl.roa;

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

import fi.vtt.dsp.service.serviceregistry.common.description.UserFeedback;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationDoesNotExistException;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationException;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemResource;
import fi.vtt.dsp.serviceframework.exceptions.ServerErrorException;
import java.util.logging.Logger;

/**
 * Individual resource for maintaining individual user feedback
 * 
 * @author ELETAI
 * @version 1.0
 * @created 31-tammi-2014 14:43:34
 */
@Path("resourcedirectory/v1/serviceregistrations/{serviceid}/servicedescription/userfeedbacks/{userfeedbackid}")
public class UserFeedbackResource
		extends
			ServiceRegistryItemResource<UserFeedback> {
    private static final Logger LOGGER = Logger.getLogger(UserFeedbackResource.class.getName());

	public UserFeedbackResource() {
		super(UserFeedback.class);
	}

	/**
	 * Returns individual item representation of this resource
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canGet(authentication)")
	public Response readUserFeedback(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId,
			@PathParam("userfeedbackid") String userFeedbackId) {
		Response resp = null;
		try {
			UserFeedback uFB = this
					.readItem(serviceId, userFeedbackId, uriInfo);
			resp = Response.ok(uFB).build();
		} catch (Exception e) {
			if (e instanceof DAONotFoundFault) {
				ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("6001");
				resp = this.convertExceptionToResponse(ex, e);
			} else {
				ServerErrorException ex = new ServerErrorException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("6001");
				resp = this.convertExceptionToResponse(ex, e);
			}
		}
		return resp;
	}

	/**
	 * Updates this item resource with the provided content
	 * 
	 * @param updatedUserFeedback
	 *            Updated item presentation of the resource
	 */
	@PUT
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@userFeedbackAuth.set(#serviceId, #userFeedbackId).canUpdate(authentication)")
    public Response updateUserFeedback(
            @PathParam("serviceid") String serviceId,
            @PathParam("userfeedbackid") String userFeedbackId,
            UserFeedback updatedUserFeedback) {

        Response response = null;

        try {
            if (updatedUserFeedback.getUserFeedbackId() == null) {
                updatedUserFeedback.setUserFeedbackId(userFeedbackId);
            }

            this.updateItem(serviceId, userFeedbackId, updatedUserFeedback);
            response = Response.ok().build();
        } catch (Exception e) {
            ServiceRegistrationException ex = new ServiceRegistrationException();
            ex.setExceptionReason(e.getMessage());
            ex.setExceptionCode("6003");
            response = this.convertExceptionToResponse(ex, e);
        }

        return response;
    }

	/**
	 * Removes this item resource
	 */
	@DELETE
	@PreAuthorize("@userFeedbackAuth.set(#serviceId, #userFeedbackId).canDelete(authentication)")
	public Response removeUserFeedback(
			@PathParam("serviceid") String serviceId,
			@PathParam("userfeedbackid") String userFeedbackId) {

		Response resp = null;
		
		try {
			this.removeItem(serviceId, userFeedbackId);
			resp = Response.ok().build();
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("6002");
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