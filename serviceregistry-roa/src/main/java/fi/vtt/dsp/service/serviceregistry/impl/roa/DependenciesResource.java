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

import fi.vtt.dsp.service.serviceregistry.common.description.Dependency;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationDoesNotExistException;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationException;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemCollectionResource;
import java.util.logging.Logger;

/**
 * Main container resource for the dependencies on the service description
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:01
 */
@Path("/resourcedirectory/v1/serviceregistrations/{serviceid}/servicedescription/dependencies")
public class DependenciesResource
		extends
			ServiceRegistryItemCollectionResource<Dependency> {
    private static final Logger LOGGER = Logger.getLogger(DependenciesResource.class.getName());

	public DependenciesResource() {
		super(Dependency.class);
	}

	/**
	 * Returns a list of the dependencies associated with the service
	 * description
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canList(authentication)")
	public Response getListOfDependencies(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId) {
		// TODO: HATEOAS linking, see ItemCollectionResource
		Response resp = null;
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		List<Dependency> depList = null;
		try {
			depList = this.readListOfItems(serviceId, null,
					queryParams.entrySet(), uriInfo);
			resp = Response.ok(depList).build();	
		} catch (Exception e) {
			if (e instanceof DAONotFoundFault) {
				ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("3001");
				resp = this.convertExceptionToResponse(ex, e);
			} else {
				ServiceRegistrationException ex = new ServiceRegistrationException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("3001");
				resp = this.convertExceptionToResponse(ex, e);
			}
		}
		return resp;
	}

	/**
	 * Creates a new dependency for the service description. Return URI to the
	 * created dependency.
	 * 
	 * @param dependency
	 *            Dependency requested to be created
	 */
	@POST
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canUpdate(authentication)")
	public Response createDependency(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId, Dependency newDependency) {
		// TODO: HATEOAS linking, see ItemCollectionResource
		Response resp = null;
		String newDependencyId;
		try {
			newDependencyId = this.createItem(serviceId, newDependency);
			UriBuilder createdServiceURI = uriInfo.getAbsolutePathBuilder();
			createdServiceURI.path(newDependencyId);
			resp = Response.created(createdServiceURI.build()).build();
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("3002");
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