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

import fi.vtt.dsp.service.serviceregistry.common.description.ServiceDescription;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationException;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationDoesNotExistException;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemResource;
import java.util.logging.Logger;

/**
 * Main resource of the individual service description
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:02
 */

@Path("/resourcedirectory/v1/serviceregistrations/{serviceid}/servicedescription")
public class ServiceDescriptionResource
		extends
			ServiceRegistryItemResource<ServiceDescription> {
    private static final Logger LOGGER = Logger.getLogger(ServiceDescriptionResource.class.getName());

	public ServiceDescriptionResource() {
		super(ServiceDescription.class);
	}

	/**
	 * Returns the identified service's description
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canGet(authentication)")
	public Response getServiceDescription(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId) {
		Response resp = null;
		try {
			ServiceDescription serviceDesc = this.readItem(serviceId, uriInfo);
			resp = Response.ok(serviceDesc).build();
		} catch (Exception e) {
			if (e instanceof DAONotFoundFault) {
				ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("4001");
				resp = this.convertExceptionToResponse(ex, e);
			} else {
				ServiceRegistrationException ex = new ServiceRegistrationException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("4001");
				resp = this.convertExceptionToResponse(ex, e);
			}
		}
		return resp;
	}

	/**
	 * Updates the identified service's description in the service registry
	 * 
	 * @param updatedDescription
	 *            Updated service description
	 */
	@PUT
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canUpdate(authentication)")
	public Response updateDescription(@PathParam("serviceid") String serviceId,
			ServiceDescription updatedDescription) {
		Response resp = null;
		try {
			this.updateItem(serviceId, updatedDescription);
			resp = Response.ok().build();
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("4002");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	/**
	 * Removes the identifies service's description from the service registry
	 */
	@DELETE
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canDelete(authentication)")
	public Response removeDescription(@PathParam("serviceid") String serviceId) {
		Response resp = null;
		try {
			this.removeItem(serviceId);
			resp = Response.ok().build();
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("4003");
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