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

import fi.vtt.dsp.service.serviceregistry.common.description.Dependency;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationDoesNotExistException;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationException;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemResource;
import java.util.logging.Logger;

/**
 * Resource for the individual dependency on the service description
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:01
 */
@Path("/resourcedirectory/v1/serviceregistrations/{serviceid}/servicedescription/dependencies/{dependencyid}")
public class DependencyResource extends ServiceRegistryItemResource<Dependency> {
    private static final Logger LOGGER = Logger.getLogger(DependencyResource.class.getName());    
    
	public DependencyResource() {
		super(Dependency.class);
	}

	/**
	 * Returns a representation of the identified dependency on the service
	 * description
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canGet(authentication)")
	public Response getDependency(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId,
			@PathParam("dependencyid") String dependencyId) {
		Response resp = null;
		try {
			Dependency dependency = this.readItem(serviceId, dependencyId,
					uriInfo);
			resp = Response.ok(dependency).build();
		} catch (Exception e) {
			if (e instanceof DAONotFoundFault) {
				ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("3003");
				resp = this.convertExceptionToResponse(ex, e);
			} else {
				ServiceRegistrationException ex = new ServiceRegistrationException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("3003");
				resp = this.convertExceptionToResponse(ex, e);
			}
		}
		return resp;
	}

	/**
	 * Updates the identified dependency in the service description
	 * 
	 * @param updatedDependency
	 *            Updated dependency information
	 */
	@PUT
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canUpdate(authentication)")
	public Response updateDependency(@PathParam("serviceid") String serviceId,
			@PathParam("dependencyid") String dependencyId,
			Dependency updatedDependency) {
		Response resp = null;
		try {
			this.updateItem(serviceId, dependencyId, updatedDependency);
			resp = Response.ok().build();
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("3004");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	/**
	 * Removes the identified dependency from the service description
	 */
	@DELETE
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canDelete(authentication)")
	public Response removeDependency(@PathParam("serviceid") String serviceId,
			@PathParam("dependencyid") String dependencyId) {
		Response resp = null;
		try {
			this.removeItem(serviceId, dependencyId);
			resp = Response.ok().build();
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("3005");
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