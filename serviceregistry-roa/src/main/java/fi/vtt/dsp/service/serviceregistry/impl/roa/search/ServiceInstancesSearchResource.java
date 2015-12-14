package fi.vtt.dsp.service.serviceregistry.impl.roa.search;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchContext;

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceDiscoveryException;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemCollectionResource;

@Path("/resourcedirectory/v1/serviceinstances/search")
public class ServiceInstancesSearchResource
		extends
			ServiceRegistryItemCollectionResource<ServiceRegistryEntry> {
	private static final Logger LOGGER = Logger.getLogger(ServiceInstance.class
			.getName());

	public ServiceInstancesSearchResource() {
		super(ServiceRegistryEntry.class);
	}

	@GET
	@Produces("application/json")
	public Response searchServiceInstances(@Context UriInfo uriInfo,
			@Context SearchContext searchContext) {
		Response resp = null;
		SearchCondition<ServiceInstance> conditionUP = searchContext
				.getCondition(ServiceInstance.class);

		try {
			MultivaluedMap<String, String> queryParams = uriInfo
					.getQueryParameters();
			List<ServiceRegistryEntry> sRegEntries = this.readListOfItems(null,
					null, queryParams.entrySet(), uriInfo);
			List<ServiceInstance> serviceInstanceList = new ArrayList<ServiceInstance>();

			for (ServiceRegistryEntry sRegEntry : sRegEntries) {
				for (ServiceInstance serviceInstance : sRegEntry
						.getServiceInstance()) {
					serviceInstanceList.add(serviceInstance);
				}
			}

			List<ServiceInstance> serviceInstances = conditionUP
					.findAll(serviceInstanceList);

			resp = Response.ok(
					serviceInstances
							.toArray(new ServiceInstance[serviceInstances
									.size()])).build();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.toString());
			ServiceDiscoveryException ex = new ServiceDiscoveryException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("9001");
			resp = this.convertExceptionToResponse(ex, e);
		}

		return resp;
	}
}
