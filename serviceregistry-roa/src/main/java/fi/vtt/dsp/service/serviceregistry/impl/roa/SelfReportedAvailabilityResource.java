package fi.vtt.dsp.service.serviceregistry.impl.roa;

import java.util.logging.Level;
import java.util.logging.Logger;

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

import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceReportingException;
import fi.vtt.dsp.service.serviceregistry.common.instance.Availability;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemResource;
import fi.vtt.dsp.serviceframework.common.ServiceAvailability;
import fi.vtt.dsp.serviceframework.exceptions.AvailabilityOperationException;

/**
 * Availability item resource holding availability information of the given
 * service instance
 * 
 * @author ELETAI
 * @version 1.0
 * @created 05-helmi-2014 15:04:24
 */
@Path("/resourcedirectory/v1/serviceregistrations/{serviceid}/serviceinstances/{serviceinstance_id}/serviceaccessendpoint/availability/selfreported")
public class SelfReportedAvailabilityResource
		extends
			ServiceRegistryItemResource<Availability> {

	private static final Logger LOGGER = Logger
			.getLogger(SelfReportedAvailabilityResource.class.getName());

	public SelfReportedAvailabilityResource() {
		super(Availability.class);
	}

	/**
	 * Returns representation of the service instance's availability resource
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canGet(authentication)")
	public Response getSelfReportedAvailability(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId) {
		Response resp = null;
		try {
			Availability availability = this.readItem(serviceId,
					serviceInstanceId, uriInfo);
			resp = Response.ok(availability.getSelfReportedAvailability())
					.build();
		} catch (Exception e) {
			ServiceReportingException ex = new ServiceReportingException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("1009");
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
	@PUT
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canUpdate(authentication)")
	public Response updateSelfReportedAvailability(
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId,
			ServiceAvailability updatedAvailability) {
		Response resp = null;
		try {
			LOGGER.log(Level.FINE, "serviceid={0} serviceinstance_id={1}",
					new Object[]{serviceId, serviceInstanceId});
			Availability avail = new Availability();
			avail.setSelfReportedAvailability(updatedAvailability);
			this.updateItem(serviceId, serviceInstanceId, avail);
			resp = Response.ok().build();
		} catch (Exception e) {
			AvailabilityOperationException ex = new AvailabilityOperationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("1010");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	/**
	 * Removes the service instance's availability information
	 */
	@DELETE
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canDelete(authentication)")
	public Response removeSelfReportedAvailability(
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId) {
		Response resp = null;
		try {
			this.removeItem(serviceId, serviceInstanceId);
			resp = Response.ok().build();
		} catch (Exception e) {
			AvailabilityOperationException ex = new AvailabilityOperationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("1011");
			resp = this.convertExceptionToResponse(ex, e);
                        LOGGER.log(Level.SEVERE, "Unable to remove self-reported availability", e);
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