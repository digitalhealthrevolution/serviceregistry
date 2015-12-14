package fi.vtt.dsp.service.serviceregistry.impl.roa;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import fi.vtt.dsp.service.serviceregistry.common.instance.Availability;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemResource;
import fi.vtt.dsp.serviceframework.exceptions.AvailabilityNotAvailableException;
import fi.vtt.dsp.serviceframework.exceptions.AvailabilityOperationException;
import java.util.logging.Logger;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Availability item resource holding availability information of the given
 * service instance
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:01
 */
@Path("/resourcedirectory/v1/serviceregistrations/{serviceid}/serviceinstances/{serviceinstance_id}/serviceaccessendpoint/availability")
public class AvailabilityResource
		extends
			ServiceRegistryItemResource<Availability> {
    private static final Logger LOGGER = Logger.getLogger(AvailabilityResource.class.getName());

	public AvailabilityResource() {
		super(Availability.class);
	}

	/**
	 * Returns representation of the service instance's availability resource
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canGet(authentication)")
	public Response getAvailability(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceinstanceId) {
		Response resp = null;
		try {
			Availability availability = this.readItem(serviceId,
					serviceinstanceId, uriInfo);
			resp = Response.ok(availability).build();
		} catch (Exception e) {
			AvailabilityNotAvailableException ex = new AvailabilityNotAvailableException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("1004");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	/**
	 * Updates the service instance's availability resource
	 * 
	 * @param updatedAvailability
	 *            Updated availability information
	 */
	// HOX: This only for reporting selfreported availability.
	// TODO: Create PUT method with Path("/inspected) for inspected availability
	@Path("/selfreported")
	@PUT
    @Consumes({"application/xml", "application/json"})       
	@Produces("application/json")
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canUpdate(authentication)")
	public Response updateSelfReportedAvailability(
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId,
			Availability updatedAvailability) {
		Response resp = null;
		try {
			// Inspected availability is not allowed to be set here
			updatedAvailability.setInspectedAvailability(null);
			this.updateItem(serviceId, serviceInstanceId, updatedAvailability);
			resp = Response.ok().build();
		} catch (Exception e) {
			AvailabilityOperationException ex = new AvailabilityOperationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("1005");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	@Path("/inspected")
	@PUT
    @Consumes({"application/xml", "application/json"})       
	@Produces("application/json")
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canUpdate(authentication)")
	public Response updateInspectedAvailability(
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId,
			Availability updatedAvailability) {
		Response resp = null;
		try {
			// Selfreported availability is not allowed to be set here
			updatedAvailability.setSelfReportedAvailability(null);
			this.updateItem(serviceId, serviceInstanceId, updatedAvailability);
			resp = Response.ok().build();
		} catch (Exception e) {
			AvailabilityOperationException ex = new AvailabilityOperationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("1006");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	/**
	 * Removes the service instance's availability information
	 */
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canDelete(authentication)")
	public Response removeAvailability(
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId) {
		Response resp = null;
		try {
			this.removeItem(serviceId, serviceInstanceId);
			resp = Response.ok().build();
		} catch (Exception e) {
			AvailabilityOperationException ex = new AvailabilityOperationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("1007");
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