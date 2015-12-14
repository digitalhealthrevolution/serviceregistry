package fi.vtt.dsp.service.serviceregistry.impl.roa;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import fi.vtt.dsp.service.serviceregistry.common.instance.Availability;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemResource;
import fi.vtt.dsp.serviceframework.exceptions.AvailabilityNotAvailableException;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * @author ELETAI
 * @version 1.0
 * @created 05-helmi-2014 15:04:24
 */
@Path("/resourcedirectory/v1/serviceregistrations/{serviceid}/serviceinstances/{serviceinstance_id}/serviceaccessendpoint/availability/inspected")
public class InspectedAvailabilityResource
		extends
			ServiceRegistryItemResource<Availability> {

	private static final Logger LOGGER = Logger
			.getLogger(InspectedAvailabilityResource.class.getName());

	public InspectedAvailabilityResource() {
		super(Availability.class);
	}

	/**
	 * Returns the service's inspected service availability information
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canGet(authentication)")
	public Response getInspectedAvailability(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId) {
		Response resp = null;
		try {
			Availability availability = this.readItem(serviceId,
					serviceInstanceId, uriInfo);
			resp = Response.ok(availability.getInspectedAvailability()).build();
		} catch (Exception e) {
			AvailabilityNotAvailableException ex = new AvailabilityNotAvailableException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("1008");
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