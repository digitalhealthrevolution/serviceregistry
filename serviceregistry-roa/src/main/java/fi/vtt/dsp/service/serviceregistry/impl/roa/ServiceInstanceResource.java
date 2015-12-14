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

import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationException;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationDoesNotExistException;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.handlers.BindingsUpdater;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemResource;
import java.util.logging.Logger;

/**
 * Individual service instance item resource
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:02
 */

@Path("/resourcedirectory/v1/serviceregistrations/{serviceid}/serviceinstances/{serviceinstance_id}")
public class ServiceInstanceResource
		extends
			ServiceRegistryItemResource<ServiceInstance> {
    private static final Logger LOGGER = Logger.getLogger(ServiceInstanceResource.class.getName());

	public ServiceInstanceResource() {
		super(ServiceInstance.class);
	}

	/**
	 * Returns representation of the identified service instance on the service
	 * registry
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canGet(authentication)")
	public Response getServiceInstance(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId) {
		Response resp = null;
		try {
			ServiceInstance servInst = this.readItem(serviceId,
					serviceInstanceId, uriInfo);
			resp = Response.ok(servInst).build();
		} catch (Exception e) {
			if (e instanceof DAONotFoundFault) {
				ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("5004");
				resp = this.convertExceptionToResponse(ex, e);
			} else {
				ServiceRegistrationException ex = new ServiceRegistrationException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("5004");
				resp = this.convertExceptionToResponse(ex, e);
			}
		}
		return resp;
	}

	/**
	 * Updates the given service instance description on the service registry
	 * 
	 * @param updatedServiceInstance
	 *            Updated service instance description information
	 */
	@PUT
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canUpdate(authentication)")
	public Response updateServiceInstance(
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId,
			ServiceInstance updatedServiceInstance) {
		Response resp = null;
		try {
			this.updateItem(serviceId, serviceInstanceId,
					updatedServiceInstance);
			resp = Response.ok().build();

			BindingsUpdater.postUpdatedBindings(serviceId, serviceInstanceId);
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("5005");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	/**
	 * Removes identified service instance from the service registry
	 */
	@DELETE
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canDelete(authentication)")
	public Response removeServiceInstance(
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId) {
		Response resp = null;
		try {
			BindingsUpdater.removeOrphanBindings(serviceId, serviceInstanceId);
			this.removeItem(serviceId, serviceInstanceId);
			resp = Response.ok().build();
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("5006");
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