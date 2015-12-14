package fi.vtt.dsp.service.serviceregistry.impl.roa.search;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceRegistryDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.ServiceRegistryDAO;
import fi.vtt.dsp.serviceframework.common.Binding;

@Path("/resourcedirectory/v1/bindings/search")
public class BindingsSearchResource {

	private static final Logger LOGGER = Logger
			.getLogger(BindingsSearchResource.class.getName());

	private ServiceRegistryDAO serviceRegistryDAO;

	public BindingsSearchResource() {
		try {
			this.serviceRegistryDAO = new MongoDBServiceRegistryDAO();
		} catch (DAOGeneralSystemFault e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
	}

	@GET
	@Produces("application/json")
	public Response searchBindings(@Context UriInfo uriInfo,
			@Context SearchContext searchContext) {
		Response resp = null;

		try {
			SearchCondition<Binding> searchCondition = searchContext
					.getCondition(Binding.class);
			if (searchCondition != null && this.serviceRegistryDAO != null) {
				List<Binding> result = new ArrayList<Binding>();
				List<ServiceRegistryEntry> serviceRegistryEntries = this.serviceRegistryDAO
						.getAll();
				for (ServiceRegistryEntry serviceRegistryEntry : serviceRegistryEntries) {
					List<ServiceInstance> serviceInstances = serviceRegistryEntry
							.getServiceInstance();
					if (serviceInstances != null && !serviceInstances.isEmpty()) {
						for (ServiceInstance serviceInstance : serviceInstances) {
							List<Binding> bindings = searchCondition
									.findAll(serviceInstance
											.getServiceAccessEndPoint()
											.getBinding());
							if (bindings != null && !bindings.isEmpty()) {
								for (Binding b : bindings) {
									result.add(b);
								}
							}
						}
					}
				}
				resp = Response.ok(result.toArray(new Binding[result.size()]))
						.build();
			}
		} catch (DAOGeneralSystemFault e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
			return Response.serverError().build();
		}
		return resp;
	}

	@GET
	@Path("/all")
	@Produces("application/json")
	public Response getAllBindings(@Context UriInfo uriInfo,
			@Context SearchContext searchContext) {
		Response resp = null;

		try {
			if (this.serviceRegistryDAO != null) {
				List<Binding> result = new ArrayList<Binding>();
				List<ServiceRegistryEntry> serviceRegistryEntries = this.serviceRegistryDAO
						.getAll();
				for (ServiceRegistryEntry serviceRegistryEntry : serviceRegistryEntries) {
					List<ServiceInstance> serviceInstances = serviceRegistryEntry
							.getServiceInstance();
					if (serviceInstances != null && !serviceInstances.isEmpty()) {
						for (ServiceInstance serviceInstance : serviceInstances) {
							List<Binding> bindings = serviceInstance
									.getServiceAccessEndPoint().getBinding();
							if (bindings != null && !bindings.isEmpty()) {
								for (Binding b : bindings) {
									result.add(b);
								}
							}
						}
					}
				}
				resp = Response.ok(result.toArray(new Binding[result.size()]))
						.build();
			}
		} catch (DAOGeneralSystemFault e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
			return Response.serverError().build();
		}
		return resp;
	}

	@GET
	@Path("/authorizedby/{userid}")
	@Produces("application/json")
	public Response getBindingsAuthorizedBy(@Context UriInfo uriInfo,
			@PathParam("userid") String userId) {
		Response resp = null;

		try {
			if (this.serviceRegistryDAO != null) {
				List<Binding> result = new ArrayList<Binding>();
				List<ServiceRegistryEntry> serviceRegistryEntries = this.serviceRegistryDAO
						.getAll();
				for (ServiceRegistryEntry serviceRegistryEntry : serviceRegistryEntries) {
					List<ServiceInstance> serviceInstances = serviceRegistryEntry
							.getServiceInstance();
					if (serviceInstances != null && !serviceInstances.isEmpty()) {
						for (ServiceInstance serviceInstance : serviceInstances) {
							List<Binding> bindings = serviceInstance
									.getServiceAccessEndPoint().getBinding();
							if (bindings != null && !bindings.isEmpty()) {
								for (Binding b : bindings) {
									if (b.getAuthorizedByUserId() != null
											&& b.getAuthorizedByUserId()
													.equals(userId)) {
										result.add(b);
									}
								}
							}
						}
					}
				}
				resp = Response.ok(result.toArray(new Binding[result.size()]))
						.build();
			}
		} catch (DAOGeneralSystemFault e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
			return Response.serverError().build();
		}
		return resp;
	}

	@GET
	@Path("/requestedby/{userid}")
	@Produces("application/json")
	public Response getBindingsRequestedBy(@Context UriInfo uriInfo,
			@PathParam("userid") String userId) {
		Response resp = null;

		try {
			if (this.serviceRegistryDAO != null) {
				List<Binding> result = new ArrayList<Binding>();
				List<ServiceRegistryEntry> serviceRegistryEntries = this.serviceRegistryDAO
						.getAll();
				for (ServiceRegistryEntry serviceRegistryEntry : serviceRegistryEntries) {
					List<ServiceInstance> serviceInstances = serviceRegistryEntry
							.getServiceInstance();
					if (serviceInstances != null && !serviceInstances.isEmpty()) {
						for (ServiceInstance serviceInstance : serviceInstances) {
							List<Binding> bindings = serviceInstance
									.getServiceAccessEndPoint().getBinding();
							if (bindings != null && !bindings.isEmpty()) {
								for (Binding b : bindings) {
									if (b.getRequestedByUserId() != null
											&& b.getRequestedByUserId().equals(
													userId)) {
										result.add(b);
									}
								}
							}
						}
					}
				}
				resp = Response.ok(result.toArray(new Binding[result.size()]))
						.build();
			}
		} catch (DAOGeneralSystemFault e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
			return Response.serverError().build();
		}
		return resp;
	}

}