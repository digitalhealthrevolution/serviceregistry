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

import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationDoesNotExistException;
import fi.vtt.dsp.service.serviceregistry.common.instance.BindingRequestEndPoint;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemResource;
import fi.vtt.dsp.serviceframework.exceptions.BindingOperationException;
import java.util.logging.Logger;

/**
 * Binding request endpoint resource for the registered service instance
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:01
 */
@Path("/resourcedirectory/v1/serviceregistrations/{serviceid}/serviceinstances/{serviceinstance_id}/bindingendpoint")
public class BindingRequestEndpointResource
		extends
			ServiceRegistryItemResource<BindingRequestEndPoint> {
    private static final Logger LOGGER = Logger.getLogger(AvailabilityResource.class.getName());

	public BindingRequestEndpointResource() {
		super(BindingRequestEndPoint.class);
	}

	/**
	 * Returns binding request endpoint resource representation on the given
	 * service instance
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canGet(authentication)")
	public Response getBindingRequestEndpoint(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId) {
		Response resp = null;
		try {
			BindingRequestEndPoint servInst = this.readItem(serviceId,
					serviceInstanceId, uriInfo);
			resp = Response.ok(servInst).build();
		} catch (Exception e) {
			if (e instanceof DAONotFoundFault) {
				ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("2001");
				resp = this.convertExceptionToResponse(ex, e);
			} else {
				BindingOperationException ex = new BindingOperationException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("2001");
				resp = this.convertExceptionToResponse(ex, e);
			}
		}
		return resp;
	}

	/**
	 * Updates binding request endpoint resource on the given service instance
	 * 
	 * @param updatedBindingRequestEndpoint
	 *            Updated binding request endpoint information
	 */
	@PUT
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canUpdate(authentication)")
	public Response updateBindingRequestEndpoint(
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId,
			BindingRequestEndPoint updatedBindingRequestEndpoint) {
		Response resp = null;
		try {
			this.updateItem(serviceId, serviceInstanceId,
					updatedBindingRequestEndpoint);
			resp = Response.ok().build();
		} catch (Exception e) {
			BindingOperationException ex = new BindingOperationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("2002");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	/**
	 * Removes binding request endpoint resource from the given service instance
	 */
	@DELETE
	@PreAuthorize("@serviceInstanceAuth.set(#serviceId, #serviceInstanceId).canDelete(authentication)")
	public Response removeBindingRequestEndpoint(
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId) {
		Response resp = null;
		try {
			this.removeItem(serviceId, serviceInstanceId);
			resp = Response.ok().build();
		} catch (Exception e) {
			BindingOperationException ex = new BindingOperationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("2003");
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