package fi.vtt.dsp.service.serviceregistry.impl.roa;

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.impl.handlers.BindingsUpdater;
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
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotSavedFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOUpdateFailedFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceInstanceDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceRegistryDAO;
import fi.vtt.dsp.service.serviceregistry.impl.handlers.BindingsGranter;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemResource;
import fi.vtt.dsp.serviceframework.common.Binding;
import fi.vtt.dsp.serviceframework.exceptions.BindingOperationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;

/**
 * Resource for the individual binding information
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:01
 */
@Path("/resourcedirectory/v1/serviceregistrations/{serviceid}/serviceinstances/{serviceinstance_id}/serviceaccessendpoint/bindings/{bindingid}")
public class BindingResource extends ServiceRegistryItemResource<Binding> {
	private static final Logger LOGGER = Logger.getLogger(BindingResource.class
			.getName());
	public BindingResource() {
		super(Binding.class);
	}

	/**
	 * Returns individual binding resource's representation associated with the
	 * registered service instance
	 */
	@GET
	@Produces("application/json")
	@PreAuthorize("@bindingAuth.set(#serviceId, #serviceInstanceId, #bindingId).canGet(authentication)")
	public Response getBinding(@Context UriInfo uriInfo,
			@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId,
			@PathParam("bindingid") String bindingId) {
		Response resp = null;
		try {
			Binding binding = this.readItemFromContainerInContainer(serviceId,
					serviceInstanceId, bindingId, uriInfo);
			resp = Response.ok(binding).build();
		} catch (Exception e) {
			if (e instanceof DAONotFoundFault) {
				ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("2004");
				resp = this.convertExceptionToResponse(ex, e);
			} else {
				BindingOperationException ex = new BindingOperationException();
				ex.setExceptionReason(e.getMessage());
				ex.setExceptionCode("2004");
				resp = this.convertExceptionToResponse(ex, e);
			}
		}
		return resp;
	}

	/**
	 * Updates the identified binding on the registered service instance
	 * 
	 * @param updatedBinding
	 *            Updated binding information
	 */
	@PUT
    @Consumes({"application/xml", "application/json"})   
	@PreAuthorize("@bindingAuth.set(#serviceId, #serviceInstanceId, #bindingId).canUpdate(authentication)")
	public Response updateBinding(@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId,
			@PathParam("bindingid") String bindingId, Binding updatedBinding) {
		Response resp = null;
		try {
			this.updateItemInContainerInContainer(serviceId, serviceInstanceId,
					bindingId, updatedBinding);
			resp = Response.ok().build();

			BindingsUpdater.postUpdatedBindings(serviceId, serviceInstanceId);
		} catch (Exception e) {
			BindingOperationException ex = new BindingOperationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("2005");
			resp = this.convertExceptionToResponse(ex, e);
		}

		return resp;
	}

	/**
	 * Removes the identified binding from the registered service instance
	 */
	@DELETE
	@PreAuthorize("@bindingAuth.set(#serviceId, #serviceInstanceId, #bindingId).canDelete(authentication)")
	public Response removeBinding(@PathParam("serviceid") String serviceId,
			@PathParam("serviceinstance_id") String serviceInstanceId,
			@PathParam("bindingid") String bindingId) {
		Response resp = null;
		try {
			this.removeItem(serviceId, serviceInstanceId, bindingId);
			resp = Response.ok().build();

			BindingsUpdater.postUpdatedBindings(serviceId, serviceInstanceId);
		} catch (Exception e) {
			BindingOperationException ex = new BindingOperationException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("2006");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}
	
	@GET
	@Path("/grant/{grantAccess}/verification/{verificationCode}")
	public Response grantBinding(@PathParam("serviceid") String serviceId, @PathParam("serviceinstance_id") String serviceInstanceId, @PathParam("bindingid") String bindingId,
	@PathParam("grantAccess") boolean grantAccess, @PathParam("verificationCode") String verficationCode) {
		LOGGER.log(Level.INFO, "grantBinding. access: " + grantAccess, " verfication code: " + verficationCode);
		Response response;
		
		try {
			MongoDBServiceRegistryDAO serviceRegistryDAO = new MongoDBServiceRegistryDAO();
			MongoDBServiceInstanceDAO mongoDBServiceInstanceDAO = new MongoDBServiceInstanceDAO();
			
			ServiceRegistryEntry serviceRegistryEntry = serviceRegistryDAO.findServiceRegistryEntry(serviceId);
			Binding binding = mongoDBServiceInstanceDAO.findBinding(serviceId, serviceInstanceId, bindingId);
			
			if (BindingsGranter.checkVerificationCode(serviceRegistryEntry.getServiceDescription().getCreatedByUserId(), bindingId, verficationCode)) {
				if (grantAccess) {
					binding.setAuthorizedByUserId(serviceRegistryEntry.getServiceDescription().getCreatedByUserId());
					binding.setStatusActive(true);
					binding.setStatusPending(false);
					binding.setStatusAuthorized(true);
					binding.setStatusRequested(false);	
					
					mongoDBServiceInstanceDAO.updateBinding(serviceId, serviceInstanceId, bindingId, binding);
				}
				else {
					mongoDBServiceInstanceDAO.deleteBinding(serviceId, serviceInstanceId, bindingId);				
				}
				
				BindingsUpdater.postUpdatedBindings(serviceId, serviceInstanceId);
			}
			else {
				return Response.status(Response.Status.FORBIDDEN).
					entity("Cannot update binding. Invalid verification code").type(MediaType.TEXT_PLAIN).build();
			}
		}
		catch (DAONotSavedFault e) {
			LOGGER.log(Level.SEVERE, "Cannot save binding", e);
			BindingOperationException ex = new BindingOperationException();
			ex.setExceptionReason("Error updating binding");
			ex.setExceptionCode("2007");
			response = this.convertExceptionToResponse(ex, e);
			
			return response;
		} 
		catch (DAOUpdateFailedFault e) {
			LOGGER.log(Level.SEVERE, "Cannot udate binding", e);
			BindingOperationException ex = new BindingOperationException();
			ex.setExceptionReason("Error updating binding");
			ex.setExceptionCode("2008");
			response = this.convertExceptionToResponse(ex, e);
			
			return response;
		}
		catch (DAONotFoundFault e) {
			LOGGER.log(Level.SEVERE, "Cannot find binding", e);
			BindingOperationException ex = new BindingOperationException();
			ex.setExceptionReason("Error updating binding");
			ex.setExceptionCode("2009");
			response = this.convertExceptionToResponse(ex, e);
			
			return response;
		}
		catch (DAOGeneralSystemFault e) {
			LOGGER.log(Level.SEVERE, "Error granting binding", e);
			BindingOperationException ex = new BindingOperationException();
			ex.setExceptionReason("Error updating binding");
			ex.setExceptionCode("2010");
			response = this.convertExceptionToResponse(ex, e);
			
			return response;
		}
			
		response = Response.ok("Binding updated successfully").build();
		
		return response;
	}

	@GET
	@Path("/key")
	@PreAuthorize("@bindingAuth.set(#serviceId, #serviceInstanceId, #bindingId).canGetKey(authentication)")
	public Response getBindingKey(@PathParam("serviceid") String serviceId, @PathParam("serviceinstance_id") String serviceInstanceId, @PathParam("bindingid") String bindingId) {
		Response response;
		String bindingKey;
		
		try {
			MongoDBServiceRegistryDAO serviceRegistryDAO = new MongoDBServiceRegistryDAO();
			
			ServiceRegistryEntry serviceRegistryEntry = serviceRegistryDAO.findServiceRegistryEntry(serviceId);
			bindingKey = BindingsGranter.getVerificationCode(serviceRegistryEntry.getServiceDescription().getCreatedByUserId(), bindingId);
			
			response = Response.ok(bindingKey).build();
		}
		catch (Exception e) {
			BindingOperationException ex = new BindingOperationException();
			ex.setExceptionReason("Error getting binding-key");
			ex.setExceptionCode("2011");
			response = this.convertExceptionToResponse(ex, e);
		}
		
		return response;
	}
	
	/*
	 * Explicit OPTIONS method related to that javascript cross-domain BS
	 */
	@OPTIONS
	public Response returnOptions() {
		return Response.status(Response.Status.NO_CONTENT).build();
	}

}