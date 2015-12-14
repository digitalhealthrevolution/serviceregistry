package fi.vtt.dsp.service.serviceregistry.impl.roa;

import fi.vtt.dsp.service.serviceregistry.auth.token.Agent;
import fi.vtt.dsp.service.serviceregistry.impl.handlers.BindingsUpdater;
import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.springframework.security.access.prepost.PreAuthorize;

import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationDoesNotExistException;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.handlers.BindingsGranter;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemCollectionResource;
import fi.vtt.dsp.serviceframework.common.Binding;
import fi.vtt.dsp.serviceframework.exceptions.BindingOperationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.security.core.Authentication;

/**
 * Collection resource for service instance's binding information
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:01
 */
@Path("/resourcedirectory/v1/serviceregistrations/{serviceid}/serviceinstances/{serviceinstance_id}/serviceaccessendpoint/bindings")
public class BindingsResource
		extends
			ServiceRegistryItemCollectionResource<Binding> {
	private static final Logger LOGGER = Logger.getLogger(BindingsResource.class.getName());

	public BindingsResource() {
		super(Binding.class);
	}

	/**
	 * Returns a list of the service instance's bindings
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@bindingsAuth.set(#serviceId, #serviceInstanceId).canList(authentication)")
	public Response getListOfBindings(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId) {
		// TODO: HATEOAS linking, see ItemCollectionResource
		// TODO: Other query parameters
		Response resp = null;
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		List<Binding> depList = null;
		try {
			depList = this.readListOfItems(serviceId, serviceInstanceId,
					queryParams.entrySet(), uriInfo);
			resp = Response.ok(depList).build();
		} catch (Exception e) {
			if (e instanceof DAONotFoundFault) {
				ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("2007");
				resp = this.convertExceptionToResponse(ex, e);
			} else {
				BindingOperationException ex = new BindingOperationException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("2007");
				resp = this.convertExceptionToResponse(ex, e);
			}
		}
		return resp;
	}

	/**
	 * Creates a new binding item to the binding collection. Return URI to the
	 * created binding resource.
	 * 
	 * @param createdBinding
	 *            Binding information to be reported
	 */
	public URI createBinding(Binding createdBinding) {
		return null;
	}

	@POST
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@bindingsAuth.set(#serviceId, #serviceInstanceId).canCreate(authentication)")
	public Response createBinding(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId,
			Binding newBinding) {
		// TODO: HATEOAS linking, see ItemCollectionResource
		Response resp = null;
		String newBindingId;
		
		newBinding.setStatusActive(false);
		newBinding.setStatusAuthorized(false);		
		newBinding.setStatusPending(true);
		newBinding.setStatusRequested(true);
		
		try {
			newBindingId = this.createItemInContainerInContainer(serviceId,
					serviceInstanceId, newBinding);
			UriBuilder createdBindingURI = uriInfo.getAbsolutePathBuilder();
			createdBindingURI.path(newBindingId);
			resp = Response.created(createdBindingURI.build()).build();

			BindingsUpdater.postUpdatedBindings(serviceId, serviceInstanceId);
			BindingsGranter.sendMail(serviceId, serviceInstanceId, newBinding);
		} catch (Exception e) {
			BindingOperationException ex = new BindingOperationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("2008");
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
	
	private String getAgentId(Authentication auth) {	
		String userId = null;

		if (auth != null) {
			try {
				Agent agent = (Agent) auth.getDetails();
				userId = agent.getId();
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Cannot get agent-id", e);
			}
		}
		
		return userId;
	}
	
	private Agent getAgent(Authentication auth) {	
		Agent agent = null;

		if (auth != null) {
			try {
				agent = (Agent) auth.getDetails();
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Cannot get agent-id", e);
			}
		}
		
		return agent;
	}
}