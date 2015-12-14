package fi.vtt.dsp.service.serviceregistry.impl.roa;

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

import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationException;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationDoesNotExistException;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemCollectionResource;
import java.util.logging.Logger;

/**
 * Service instance collection resource
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:02
 */
@Path("/resourcedirectory/v1/serviceregistrations/{serviceid}/serviceinstances")
public class ServiceInstancesResource
		extends
			ServiceRegistryItemCollectionResource<ServiceInstance> {
	private static final Logger LOGGER = Logger.getLogger(ServiceInstancesResource.class.getName());

	public ServiceInstancesResource() {
		super(ServiceInstance.class);
	}

	/**
	 * Returns a list of the registered service instances on the service
	 * registry
	 */
	@GET
	@Produces("application/json")
	// TODO: To enable XML support check the
	// common.serviceregistry.service.dsp.vtt.fi.xsd for proper @XMLRootElement
	// generation
	// I.e. change ServiceREgistryEntry complexType to an element
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canGet(authentication)")
	public Response getListOfServiceInstances(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId) {
		// TODO: HATEOAS linking, see ItemCollectionResource
		Response resp = null;
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();

		List<ServiceInstance> depList = null;
		try {
			depList = this.readListOfItems(serviceId, null,
					queryParams.entrySet(), uriInfo);
			resp = Response.ok(depList).build();
		} catch (Exception e) {
			if (e instanceof DAONotFoundFault) {
				ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("5007");
				resp = this.convertExceptionToResponse(ex, e);
			} else {
				ServiceRegistrationException ex = new ServiceRegistrationException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("5007");
				resp = this.convertExceptionToResponse(ex, e);
			}
		}
		return resp;
	}

	/**
	 * Returns a list of the registered service instances on the service
	 * registry authored by the given user
	 */
	@GET
	@Path("/authoredby/{userid}")
	@Produces("application/json")
	// TODO: To enable XML support check the
	// common.serviceregistry.service.dsp.vtt.fi.xsd for proper @XMLRootElement
	// generation
	// I.e. change ServiceREgistryEntry complexType to an element
	public Response getServiceInstancesAuthoredBy(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId,
			@PathParam("userid") String userId) {
		// TODO: HATEOAS linking, see ItemCollectionResource
		Response resp = null;
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();

		List<ServiceInstance> depList = null;
		try {
			depList = this.readListOfItems(serviceId, null,
					queryParams.entrySet(), uriInfo, userId);
			if (depList != null) {
				for (int i = depList.size(); i >= 0; i--) {
					ServiceInstance si = (ServiceInstance) depList.get(i);
					if (si.getCreatedByUserId() == null
							|| !si.getCreatedByUserId().equals(userId)) {
						depList.remove(i);
					}
				}
			}
			resp = Response.ok(depList).build();
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("5008");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	/**
	 * Creates a new individual service instance on the service registry. Return
	 * URI to the created service instance.
	 * 
	 * @param newServiceInstance
	 *            Service instance information being registered
	 */
	@POST
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@serviceInstancesAuth.set(#serviceId).canCreate(authentication)")
	public Response createServiceInstance(@Context UriInfo uriinfo,
			@PathParam("serviceid") String serviceId,
			ServiceInstance newServiceInstance) {
		// TODO: HATEOAS linking, see ItemCollectionResource
		Response resp = null;
		String newServiceInstanceId;
		try {
			newServiceInstanceId = this.createItem(serviceId,
					newServiceInstance);
			UriBuilder createdServiceURI = uriinfo.getAbsolutePathBuilder();
			createdServiceURI.path(newServiceInstanceId);
			resp = Response.created(createdServiceURI.build()).build();
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("5009");
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