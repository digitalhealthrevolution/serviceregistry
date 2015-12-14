package fi.vtt.dsp.service.serviceregistry.impl.roa.search;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.description.TechnicalServiceDescription;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceRegistryDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.ServiceRegistryDAO;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceDiscoveryException;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemCollectionResource;

@Path("/resourcedirectory/v1/technicaldescriptions/search")
public class TechnicalServiceDescriptionsSearchResource
		extends
			ServiceRegistryItemCollectionResource<TechnicalServiceDescription> {
	private static final Logger LOGGER = Logger
			.getLogger(TechnicalServiceDescriptionsSearchResource.class
					.getName());
	private ServiceRegistryDAO serviceRegistryDAO;

	public TechnicalServiceDescriptionsSearchResource() {
		super(TechnicalServiceDescription.class);
	}

	@GET
	@Produces("application/json")
	public Response searchTechnicalServiceDescriptions(
			@Context UriInfo uriInfo, @Context SearchContext searchContext) {
		Response response = null;
		SearchCondition<TechnicalServiceDescription> searchCondition = searchContext
				.getCondition(TechnicalServiceDescription.class);

		LOGGER.log(Level.INFO, "searchTechnicalServiceDescriptions start");

		try {
			serviceRegistryDAO = new MongoDBServiceRegistryDAO();
			List<TechnicalServiceDescription> technicalServiceDescriptions = new ArrayList<TechnicalServiceDescription>();
			List<TechnicalServiceDescription> returnTechnicalServiceDescriptions;
			LOGGER.log(Level.INFO,
					"Number of technical service descriptions in database: "
							+ technicalServiceDescriptions.size());

			List<ServiceRegistryEntry> serviceRegistryEntries = serviceRegistryDAO
					.getAll();

			for (ServiceRegistryEntry serviceRegistryEntry : serviceRegistryEntries) {
				for (TechnicalServiceDescription technicalServiceDescription : serviceRegistryEntry
						.getServiceDescription()
						.getTechnicalServiceDescription()) {
					technicalServiceDescriptions
							.add(technicalServiceDescription);
				}
			}

			returnTechnicalServiceDescriptions = searchCondition
					.findAll(technicalServiceDescriptions);

			response = Response
					.ok(returnTechnicalServiceDescriptions
							.toArray(new TechnicalServiceDescription[returnTechnicalServiceDescriptions
									.size()])).build();

			LOGGER.log(Level.INFO, "Found "
					+ returnTechnicalServiceDescriptions.size()
					+ "tecnical descriptions");
		} catch (Exception e) {
			LOGGER.log(Level.INFO, "Error searching technical descriptions "
					+ e.getMessage());
			ServiceDiscoveryException ex = new ServiceDiscoveryException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("9004");
			response = this.convertExceptionToResponse(ex, e);
		}

		return response;
	}
}
