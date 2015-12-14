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

import fi.vtt.dsp.service.serviceregistry.common.description.HumanReadableDescription;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationException;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationDoesNotExistException;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemResource;
import java.util.logging.Logger;

/**
 * Resource for maintaining the human readable description in the identified
 * service
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:01
 */

@Path("/resourcedirectory/v1/serviceregistrations/{serviceid}/servicedescription/humanreadabledescription")
public class HumanReadableDescriptionResource
		extends
			ServiceRegistryItemResource<HumanReadableDescription> {
    private static final Logger LOGGER = Logger.getLogger(DependenciesResource.class.getName());

	public HumanReadableDescriptionResource() {
		super(HumanReadableDescription.class);
	}

	/**
	 * Returns the human readable description for the identified service
	 * description
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canGet(authentication)")
	public Response getHumanReadableDescription(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId) {
		Response resp = null;
		try {
			HumanReadableDescription humanReadableDesc = this.readItem(
					serviceId, uriInfo);
			resp = Response.ok(humanReadableDesc).build();
		} catch (Exception e) {
			if (e instanceof DAONotFoundFault) {
				ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
				ex.setExceptionReason(e.getMessage());
				resp = this.convertExceptionToResponse(ex, e);
			} else {
				ServiceRegistrationException ex = new ServiceRegistrationException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("4004");
				resp = this.convertExceptionToResponse(ex, e);
			}
		}
		return resp;
	}

	/**
	 * Updates the human readable description in the identified service
	 * description
	 * 
	 * @param updatedHumanReadableDescription
	 *            Updated human readable description
	 * @return
	 */
	@PUT
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canUpdate(authentication)")
	public Response updateHumanReadableDescription(
			@PathParam("serviceid") String serviceId,
			HumanReadableDescription updatedHumanReadableDescription) {
		Response resp = null;
		try {
			this.updateItem(serviceId, updatedHumanReadableDescription);
			resp = Response.ok().build();
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("4005");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	/**
	 * Removes the human readable description from the identified service
	 * description.
	 */
	@DELETE
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canDelete(authentication)")
	public Response removeHumanReadableDescription(
			@PathParam("serviceid") String serviceId) {
		Response resp = null;
		try {
			this.removeItem(serviceId);
			resp = Response.ok().build();
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("4006");
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