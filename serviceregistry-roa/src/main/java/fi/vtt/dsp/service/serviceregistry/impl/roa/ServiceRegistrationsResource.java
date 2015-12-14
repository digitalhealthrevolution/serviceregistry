package fi.vtt.dsp.service.serviceregistry.impl.roa;

import fi.vtt.dsp.service.serviceregistry.auth.token.Agent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.springframework.security.access.prepost.PreAuthorize;

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationException;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationDoesNotExistException;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.handlers.ServiceIconDownloader;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemCollectionResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Main collection resource for the service registrations
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:02
 */

@Path("/resourcedirectory/v1/serviceregistrations")
public class ServiceRegistrationsResource
		extends
			ServiceRegistryItemCollectionResource<ServiceRegistryEntry> {

	private static final Logger LOGGER = Logger
			.getLogger(ServiceRegistrationsResource.class.getName());

	public ServiceRegistrationsResource() {
		super(ServiceRegistryEntry.class);
	}

	/**
	 * Returns a list of the registered service registrations
	 */
	@GET
	@Produces("application/json")
	// TODO: To enable XML support check the
	// common.serviceregistry.service.dsp.vtt.fi.xsd for proper @XMLRootElement
	// generation
	// I.e. change ServiceRegistryEntry complexType to an element
	@PreAuthorize("@serviceRegistrationsAuth.canList(authentication)")
	public Response getListOfRegistrations(@Context UriInfo uriInfo) {

		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		String userId = null;

		if (auth != null) {
			try {
				Agent agent = (Agent) (Object) auth.getDetails();
				userId = agent.getId();
			} catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unable to get authentication details", e);
			}
		}

		// TODO: HATEOAS linking, see ItemCollectionResource
		// MAnage fields attribute
		Response resp = null;
		try {
			MultivaluedMap<String, String> queryParams = uriInfo
					.getQueryParameters();
			List<ServiceRegistryEntry> sRegEntry = this.readListOfItems(null,
					null, queryParams.entrySet(), uriInfo, userId);
			resp = Response
					.ok(sRegEntry.toArray(new ServiceRegistryEntry[sRegEntry
							.size()])).build();
		}

		catch (Exception e) {
			if (e instanceof DAONotFoundFault) {
				ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("5013");
				resp = this.convertExceptionToResponse(ex, e);
			} else {
				ServiceRegistrationException ex = new ServiceRegistrationException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("5013");
				resp = this.convertExceptionToResponse(ex, e);
			}
		}
		return resp;
	}

	/**
	 * Creates a new service registration to the service registry
	 * 
	 * @param registrationEntry
	 *            Service registry entry being created
	 */
	@POST
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@serviceRegistrationsAuth.canCreate(authentication)")
	public Response createRegistration(@Context UriInfo uriInfo,
			ServiceRegistryEntry registrationEntry) {
		// TODO: HATEOAS linking, see ItemCollectionResource
		Response resp = null;
		String newServiveIdURIpart;
		try {
			newServiveIdURIpart = this.createItem(null, registrationEntry);
			UriBuilder createdServiceURI = uriInfo.getAbsolutePathBuilder();
			createdServiceURI.path(newServiveIdURIpart);
			resp = Response.created(createdServiceURI.build()).build();

			ServiceIconDownloader.downloadIcon(registrationEntry,
					newServiveIdURIpart, false);
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("5014");
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