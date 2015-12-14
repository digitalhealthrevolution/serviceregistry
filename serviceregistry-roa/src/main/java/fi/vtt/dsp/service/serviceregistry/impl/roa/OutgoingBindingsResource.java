package fi.vtt.dsp.service.serviceregistry.impl.roa;

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceRegistryDAO;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemCollectionResource;
import fi.vtt.dsp.serviceframework.common.Binding;
import fi.vtt.dsp.serviceframework.exceptions.ServerErrorException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/resourcedirectory/v1/serviceregistrations/{serviceid}/serviceinstances/{serviceinstance_id}/outgoingbindings")
public class OutgoingBindingsResource extends ServiceRegistryItemCollectionResource<Binding> {
	private static final Logger LOGGER = Logger.getLogger(OutgoingBindingsResource.class.getName());

	public OutgoingBindingsResource() {
		super(Binding.class);
	}
	
	private static class OutgoingBinding {
		
		private String serviceId;
		private String serviceInstanceId;
		private Binding binding;

		public String getServiceId() {
			return serviceId;
		}
		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}
		public String getServiceInstanceId() {
			return serviceInstanceId;
		}
		public void setServiceInstanceId(String serviceInstanceId) {
			this.serviceInstanceId = serviceInstanceId;
		}		
		public Binding getBinding() {
			return binding;
		}
		public void setBinding(Binding binding) {
			this.binding = binding;
		}
	}

	@GET
	@Produces("application/json")
	public Response createBinding(@Context UriInfo uriInfo, @PathParam("serviceid") String serviceId, @PathParam("serviceinstance_id") String serviceInstanceId) {
		Response response = null;

		try {
			MongoDBServiceRegistryDAO serviceDAO = new MongoDBServiceRegistryDAO();
			List<ServiceRegistryEntry> services = serviceDAO.getAll();
			ArrayList<OutgoingBinding> outgoingBindings = new ArrayList<>();
			
			for (ServiceRegistryEntry serviceRegistryEntry : services) {
				for (ServiceInstance serviceInstance : serviceRegistryEntry.getServiceInstance()) {
					for (Binding binding : serviceInstance.getServiceAccessEndPoint().getBinding()) {
						if (binding.getBoundByServiceInstanceId().equals(serviceInstanceId)) {
							OutgoingBinding ogb = new OutgoingBinding();
							ogb.setServiceId(serviceRegistryEntry.getServiceId());
							ogb.setServiceInstanceId(serviceInstance.getServiceInstanceId());
							ogb.setBinding(binding);
							outgoingBindings.add(ogb);
						}
					}
				}
			}
			
			response = Response.ok(outgoingBindings.toArray(new OutgoingBinding[outgoingBindings.size()])).build();
		}
		catch (Exception e) {
			ServerErrorException ex = new ServerErrorException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("7005");
			response = this.convertExceptionToResponse(ex, e);
		}

		return response;
	}
}
