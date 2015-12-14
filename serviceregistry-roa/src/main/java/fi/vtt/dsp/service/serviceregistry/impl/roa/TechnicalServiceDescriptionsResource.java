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

import fi.vtt.dsp.service.serviceregistry.common.description.TechnicalServiceDescription;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationException;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationDoesNotExistException;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemCollectionResource;
import java.util.logging.Logger;

/**
 * Main container resource for the technical service descriptions on the service
 * description
 * 
 * @author ELETAI
 * @version 1.0
 * @created 09-tammi-2014 16:07:12
 */

@Path("resourcedirectory/v1/serviceregistrations/{serviceid}/servicedescription/technicaldescriptions")
public class TechnicalServiceDescriptionsResource
		extends
			ServiceRegistryItemCollectionResource<TechnicalServiceDescription> {
    private static final Logger LOGGER = Logger.getLogger(TechnicalServiceDescriptionsResource.class.getName());

	public TechnicalServiceDescriptionsResource() {
		super(TechnicalServiceDescription.class);
	}

	/**
	 * Returns a list of the technical service descriptions associated with the
	 * service description
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canGet(authentication)")
	public Response getListOfTechnicalServiceDescriptions(
			@Context UriInfo uriInfo, @PathParam("serviceid") String serviceId) {
		// TODO: HATEOAS linking, see ItemCollectionResource
		Response resp = null;
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		List<TechnicalServiceDescription> tDescList = null;
		try {
			tDescList = this.readListOfItems(serviceId, null,
					queryParams.entrySet(), uriInfo);
			resp = Response.ok(tDescList).build();
		} catch (Exception e) {
			if (e instanceof DAONotFoundFault) {
				ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("4010");
				resp = this.convertExceptionToResponse(ex, e);
			} else {
				ServiceRegistrationException ex = new ServiceRegistrationException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("4010");
				resp = this.convertExceptionToResponse(ex, e);
			}
		}
		return resp;

	}

	/**
	 * Creates a new technical service description for the service description.
	 * Returns URI to the created dependency.
	 * 
	 * @param newTechnicalServiceDescription
	 */
	@POST
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canUpdate(authentication)")
	public Response createTechnicalServiceDescription(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId,
			TechnicalServiceDescription newTechnicalServiceDescription) {
		// TODO: HATEOAS linking, see ItemCollectionResource
		Response resp = null;
		String newTechnicalServiceDescriptionId;
		try {
			newTechnicalServiceDescriptionId = this.createItem(serviceId,
					newTechnicalServiceDescription);
			UriBuilder createdServiceURI = uriInfo.getAbsolutePathBuilder();
			createdServiceURI.path(newTechnicalServiceDescriptionId);
			resp = Response.created(createdServiceURI.build()).build();
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("4011");
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