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

import fi.vtt.dsp.service.serviceregistry.common.instance.AvailabilityRequestEndPoint;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemResource;
import fi.vtt.dsp.serviceframework.exceptions.AvailabilityNotAvailableException;
import fi.vtt.dsp.serviceframework.exceptions.AvailabilityOperationException;
import java.util.logging.Logger;

/**
 * Availability request endpoint resource for the registered service instance
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:01
 */
@Path("/resourcedirectory/v1/serviceregistrations/{serviceid}/serviceinstances/{serviceinstance_id}/availabilityendpoint")
public class AvailabilityRequestEndpointResource
		extends
			ServiceRegistryItemResource<AvailabilityRequestEndPoint> {
    private static final Logger LOGGER = Logger.getLogger(AvailabilityRequestEndpointResource.class.getName());

	public AvailabilityRequestEndpointResource() {
		super(AvailabilityRequestEndPoint.class);
	}

	/**
	 * Returns availability request endpoint resource representation on the
	 * given service instance
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canGet(authentication)")
	public Response getAvailabilityRequestEndpoint(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId) {
		Response resp = null;
		try {
			AvailabilityRequestEndPoint servInst = this.readItem(serviceId,
					serviceInstanceId, uriInfo);
			resp = Response.ok(servInst).build();
		} catch (Exception e) {
			AvailabilityNotAvailableException ex = new AvailabilityNotAvailableException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("1001");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	/**
	 * Updates availability request endpoint resource on the given service
	 * instance
	 * 
	 * @param updatedAvailabilityRequestEndpoint
	 *            Updated availability request endpoint information
	 */
	@PUT
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canUpdate(authentication)")
	public Response updateAvailabilityRequestEndpoint(
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId,
			AvailabilityRequestEndPoint updatedAvailabilityRequestEndpoint) {
		Response resp = null;
		try {
			this.updateItem(serviceId, serviceInstanceId,
					updatedAvailabilityRequestEndpoint);
			resp = Response.ok().build();
		} catch (Exception e) {
			AvailabilityOperationException ex = new AvailabilityOperationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("1002");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	/**
	 * Removes availability request endpoint resource from the given service
	 * instance
	 */
	@DELETE
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canDelete(authentication)")
	public Response removeAvailabilityRequestEndpoint(
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId) {
		Response resp = null;
		try {
			this.removeItem(serviceId, serviceInstanceId);
			resp = Response.ok().build();
		} catch (Exception e) {
			AvailabilityOperationException ex = new AvailabilityOperationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("1003");
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