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

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationException;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationDoesNotExistException;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.handlers.BindingsUpdater;
import fi.vtt.dsp.service.serviceregistry.impl.handlers.ServiceIconDownloader;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemResource;
import java.util.logging.Logger;

/**
 * Individual service registration item resource
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:02
 */

@Path("/resourcedirectory/v1/serviceregistrations/{serviceid}")
public class ServiceRegistrationResource
		extends
			ServiceRegistryItemResource<ServiceRegistryEntry> {
    private static final Logger LOGGER = Logger.getLogger(ServiceRegistrationResource.class.getName());

	public ServiceRegistrationResource() {
		super(ServiceRegistryEntry.class);
	}

	/**
	 * Returns a resource representation of the identified service registration
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canGet(authentication)")
	public Response getRegistration(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId) {
		Response resp = null;
		try {
			ServiceRegistryEntry sRegEntry = this.readItem(serviceId, uriInfo);
			resp = Response.ok(sRegEntry).build();
		} catch (Exception e) {
			if (e instanceof DAONotFoundFault) {
				ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("5010");
				resp = this.convertExceptionToResponse(ex, e);
			} else {
				ServiceRegistrationException ex = new ServiceRegistrationException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("5010");
				resp = this.convertExceptionToResponse(ex, e);
			}
		}
		return resp;
	}

	/**
	 * Updates the identified service's registrations
	 * 
	 * @param updatedServiceRegistration
	 *            Updated service registration
	 */
	@PUT
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canUpdate(authentication)")
	public Response updateRegistration(
			@PathParam("serviceid") String serviceId,
			ServiceRegistryEntry updatedServiceRegistration) {
		Response resp = null;
		try {
			this.updateItem(serviceId, updatedServiceRegistration);
			resp = Response.ok().build();

			ServiceIconDownloader.downloadIcon(updatedServiceRegistration, true);
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("5011");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	/**
	 * Removes the identified service registration from the service registry
	 */
	@DELETE
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canDelete(authentication)")
	public Response removeRegistration(@PathParam("serviceid") String serviceId) {
		Response resp = null;
		try {
			BindingsUpdater.removeOrphanBindings(serviceId);
			this.removeItem(serviceId);
			resp = Response.ok().build();
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("5012");
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