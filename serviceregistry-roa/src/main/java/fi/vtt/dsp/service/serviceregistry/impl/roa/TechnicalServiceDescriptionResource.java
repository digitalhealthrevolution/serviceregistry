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

import fi.vtt.dsp.service.serviceregistry.common.description.TechnicalServiceDescription;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationException;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationDoesNotExistException;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemResource;
import java.util.logging.Logger;

/**
 * Resource for maintaining the technical service description in the identified
 * service
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:02
 */
@Path("/resourcedirectory/v1/serviceregistrations/{serviceid}/servicedescription/technicaldescriptions/{technicaldescriptionid}")
public class TechnicalServiceDescriptionResource
		extends
			ServiceRegistryItemResource<TechnicalServiceDescription> {
    private static final Logger LOGGER = Logger.getLogger(TechnicalServiceDescriptionResource.class.getName());

	public TechnicalServiceDescriptionResource() {
		super(TechnicalServiceDescription.class);
	}

	/**
	 * Returns the technical service description for the identified service
	 * description
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canGet(authentication)")
	public Response getTechnicalServiceDescirption(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId,
			@PathParam("technicaldescriptionid") String technicalDescriptionId) {
		Response resp = null;
		try {
			TechnicalServiceDescription techDesc = this.readItem(serviceId,
					technicalDescriptionId, uriInfo);
			resp = Response.ok(techDesc).build();
		} catch (Exception e) {
			if (e instanceof DAONotFoundFault) {
				ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("4007");
				resp = this.convertExceptionToResponse(ex, e);
			} else {
				ServiceRegistrationException ex = new ServiceRegistrationException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("4007");
				resp = this.convertExceptionToResponse(ex, e);
			}
		}
		return resp;
	}

	/**
	 * Updates the technical service description in the identified service
	 * description
	 * 
	 * @param updatedTechnicalDescription
	 *            Updated technical service description information
	 */
	@PUT
    @Consumes("application/json")   
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canUpdate(authentication)")
	public Response updateTechnicalServiceDescirption(
			@PathParam("serviceid") String serviceId,
			@PathParam("technicaldescriptionid") String technicalDescriptionId,
			TechnicalServiceDescription updatedTechnicalDescription) {
		Response resp = null;
		try {
			this.updateItem(serviceId, technicalDescriptionId,
					updatedTechnicalDescription);
			resp = Response.ok().build();
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("4008");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	/**
	 * Removes the technical service description from the identified service
	 * description.
	 */
	@DELETE
	@PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canDelete(authentication)")
	public Response removeTechnicalServiceDescirption(
			@PathParam("serviceid") String serviceId,
			@PathParam("technicaldescriptionid") String technicalDescriptionId) {
		Response resp = null;
		try {
			this.removeItem(serviceId, technicalDescriptionId);
			resp = Response.ok().build();
		} catch (Exception e) {
			ServiceRegistrationException ex = new ServiceRegistrationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("4009");
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