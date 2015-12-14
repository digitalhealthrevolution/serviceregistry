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
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemResource;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service access endpoint resource for the registered service instance
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:02
 */
@Path("/resourcedirectory/v1/serviceregistrations/{serviceid}/serviceinstances/{serviceinstance_id}/serviceaccessendpoint")
public class ServiceAccessEndpointResource
		extends
			ServiceRegistryItemResource<ServiceAccessEndPoint> {
    private static final Logger LOGGER = Logger.getLogger(ServiceAccessEndpointResource.class.getName());

	public ServiceAccessEndpointResource() {
		super(ServiceAccessEndPoint.class);
	}

	/**
	 * Returns service access endpoint resource representation on the given
	 * service instance
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canGet(authentication)")
	public Response getServiceAccessEndpoint(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId) {
		Response resp = null;
		try {
			ServiceAccessEndPoint servInst = this.readItem(serviceId,
					serviceInstanceId, uriInfo);
			resp = Response.ok(servInst).build();
		} catch (Exception e) {
			if (e instanceof DAONotFoundFault) {
				ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("5001");
				resp = this.convertExceptionToResponse(ex, e);
			} else {
				ServiceRegistrationException ex = new ServiceRegistrationException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("5001");
				resp = this.convertExceptionToResponse(ex, e);
			}
                        
                        LOGGER.log(Level.SEVERE, "Unable to get service-access-endpoint", e);
		}
		return resp;
	}

	/**
	 * Updates service access endpoint resource on the given service instance
	 * 
	 * @param updatedServiceAccessPoint
	 *            Updated service access endpoint information
	 */
	@PUT
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canUpdate(authentication)")
	public Response updateServiceAccessEndpoint(
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId,
			ServiceAccessEndPoint updatedServiceAccessPoint) {
		Response resp = null;
		try {
			this.updateItem(serviceId, serviceInstanceId,
					updatedServiceAccessPoint);
			resp = Response.ok().build();
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("5002");
			resp = this.convertExceptionToResponse(ex, e);
                        LOGGER.log(Level.SEVERE, "Unable to update service-access-endpoint", e);
		}
		return resp;
	}
	/**
	 * Removes service access endpoint resource from the given service instance
	 */
	@DELETE
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canDelete(authentication)")
	public Response removeServiceAccessEndpoint(
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId) {
		Response resp = null;
		try {
			this.removeItem(serviceId, serviceInstanceId);
			resp = Response.ok().build();
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("5003");
			resp = this.convertExceptionToResponse(ex, e);
                        LOGGER.log(Level.SEVERE, "Unable to remove service-access-endpoint", e);
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