package fi.vtt.dsp.service.serviceregistry.impl.resourceframework;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.description.Dependency;
import fi.vtt.dsp.service.serviceregistry.common.description.TechnicalServiceDescription;
import fi.vtt.dsp.service.serviceregistry.common.description.UserFeedback;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotSavedFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOUpdateFailedFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceRegistryDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceDescriptionDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceInstanceDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.ServiceRegistryDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.ServiceDescriptionDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.ServiceInstanceDAO;
import fi.vtt.dsp.serviceframework.common.Binding;
import fi.vtt.dsp.serviceframework.exceptions.*;
import fi.vtt.dsp.serviceframework.resourceframework.ItemCollectionResource;
import fi.vtt.dsp.serviceframework.resourceframework.Link;
import fi.vtt.dsp.serviceframework.resourceframework.ResourceRepresentation;

/**
 * Resource for collection of items with URI and provided methods for content
 * creation and reading.
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:01
 */

public abstract class ServiceRegistryItemCollectionResource<T>
		extends
			ItemCollectionResource {

	private static final Logger LOGGER = Logger
			.getLogger(ServiceRegistryItemCollectionResource.class.getName());
	private ResourceRepresentation mResourceRepresentation;
	private ServiceRegistryDAO serviceRegistryDAO;
	private ServiceDescriptionDAO serviceDescriptionDAO;
	private ServiceInstanceDAO serviceInstanceDAO;
	private Class<T> t;

	public ServiceRegistryItemCollectionResource(Class<T> resourceType) {
		this.t = resourceType;
		this.setLink(new ArrayList<Link>());
		try {
			this.serviceRegistryDAO = new MongoDBServiceRegistryDAO();
			this.serviceDescriptionDAO = new MongoDBServiceDescriptionDAO();
			this.serviceInstanceDAO = new MongoDBServiceInstanceDAO();
		} catch (DAOGeneralSystemFault e) {
			this.convertExceptionToResponse(null, e);
		}
	}

	public void set(Class<T> t1) {
		this.t = t1;
	}

	public Class<T> get() {
		return this.t;
	}

	public java.util.List<Link> getLinks() {
		if (this.getLink() == null) {
			this.setLink(new ArrayList<Link>());
                }
                
		return this.getLink();
	}

	private void resetLinks() {
		if (!this.getLinks().isEmpty()) {
			this.getLinks().clear();
		}
	}

	private void addLink(Link link) {
		if (!this.getLinks().contains(link)) {
			this.getLinks().add(link);
		}
	}

	// Link as RFC 5988 Web Linking draft-nottingham-http-link-header
	public String getLinkAsNottinghamHTTPString(Link link) {
		return "<" + link.getTarget() + ">; rel=\"" + link.getRelation() + "\"";
	}

	public ResourceRepresentation getResourceRepresentation() {
		return mResourceRepresentation;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setResourceRepresentation(ResourceRepresentation newVal) {
		mResourceRepresentation = newVal;
	}

	/**
	 * Creates and adds a new item into the collection. Returns URI to the
	 * created item resources.
	 * 
	 * @param itemRepresentation
	 *            Representation of the resource to be added into the collection
	 */
	public String createItem(String collectionId, T itemRepresentation)
			throws DAOGeneralSystemFault, DAONotFoundFault, DAONotSavedFault,
			DAOUpdateFailedFault {
		String newItemId = "";
		if (this.get().equals(ServiceRegistryEntry.class)) {
			newItemId = serviceRegistryDAO
					.insertNewServiceRegistryEntry((ServiceRegistryEntry) itemRepresentation);
		}
		if (this.get().equals(TechnicalServiceDescription.class)) {
			TechnicalServiceDescription techDesc = (TechnicalServiceDescription) itemRepresentation;
			newItemId = serviceDescriptionDAO
					.insertNewTechnicalServiceDescription(collectionId,
							techDesc);
		}
		if (this.get().equals(UserFeedback.class)) {
			UserFeedback uFB = (UserFeedback) itemRepresentation;
			newItemId = serviceDescriptionDAO.insertNewUserFeedback(
					collectionId, uFB);
		}
		if (this.get().equals(Dependency.class)) {
			Dependency servDep = (Dependency) itemRepresentation;
			newItemId = serviceDescriptionDAO.insertNewDependency(collectionId,
					servDep);
		}
		if (this.get().equals(ServiceInstance.class)) {
			ServiceInstance servInst = (ServiceInstance) itemRepresentation;
			newItemId = serviceInstanceDAO.insertNewServiceInstance(
					collectionId, servInst);
		}
		return newItemId;
	}

	// Binding
	public String createItemInContainerInContainer(String collectionId,
			String containedCollectionId, T itemRepresentation)
			throws DAOGeneralSystemFault, DAONotFoundFault, DAONotSavedFault,
			DAOUpdateFailedFault {
		String newItemId = "";
		if (this.get().equals(Binding.class)) {
			newItemId = serviceInstanceDAO.insertNewBinding(collectionId,
					containedCollectionId, (Binding) itemRepresentation);
		}
		return newItemId;
	}

	private int[] parsePaginationParams(
			Set<Entry<String, List<String>>> queryParams) {
		int[] paginationPararms = new int[4];
		paginationPararms[0] = -1;
		paginationPararms[1] = -1;
		paginationPararms[2] = -1;
		paginationPararms[3] = -1;

		for (Entry<String, List<String>> entry : queryParams) {
			if (entry.getKey().equals("top")) {
				// HOX: Only the very first ?top=value is cared for
				paginationPararms[0] = Integer
						.parseInt(entry.getValue().get(0));
			}
			if (entry.getKey().equals("tail")) {
				// HOX: Only the very first ?tail=value is cared for
				paginationPararms[1] = Integer
						.parseInt(entry.getValue().get(0));
			}
			if (entry.getKey().equals("section")) {
				// HOX: Only the 0 and 1 indexed values are card
				String sectionQuery = entry.getValue().get(0);
				String[] params = sectionQuery.split("[-]");
				// HOX: Rest are discarded
				paginationPararms[2] = Integer.parseInt(params[0]);
				paginationPararms[3] = Integer.parseInt(params[1]);
			}
		}

		return paginationPararms;
	}

	private String[] parseFilteringParams(
			Set<Entry<String, List<String>>> queryParams) {
		String[] fields = null;
		for (Entry<String, List<String>> entry : queryParams) {
			if (entry.getKey().equals("fields")) {
				// One and only value string, with individual values separated
				// by commas
				String fieldString = entry.getValue().get(0);
				fields = fieldString.split("[,]");
                                LOGGER.log(Level.INFO, "field count " + fields.length);
			}
		}
		return fields;
	}

	private List<T> doListFiltering(String[] fieldsParams, List listToBeFiltered) {
		List<T> objCandidateList = new ArrayList<T>();

		for (String fieldParam : fieldsParams) {
		}
		return objCandidateList;
	}

	private List<T> doPagination(List<T> listToBePaginated,
			int[] paginationParams) {
		List<T> paginatedList = listToBePaginated;
		// top
		if (paginationParams[0] > 0) {
			if (paginationParams[0] > listToBePaginated.size()) {
				paginationParams[0] = listToBePaginated.size();
			}
			// sublist toRange seems to be exclusive therefore +1
			LOGGER.log(Level.FINE, "Returning list from 0 to {0}",
					paginationParams[0]);
			paginatedList = listToBePaginated.subList(0, (paginationParams[0]));

		}
		// tail
		if (paginationParams[1] > 0) {
			if (paginationParams[1] > listToBePaginated.size()) {
				paginationParams[1] = listToBePaginated.size();
			}
			// sublist toRange seems to be exclusive therefore +1
			LOGGER.log(Level.FINE, "Returning list from {0} to {1}",
					new Object[]{
							listToBePaginated.size() - paginationParams[1],
							listToBePaginated.size()});
			paginatedList = listToBePaginated.subList(
					(listToBePaginated.size() - paginationParams[1]),
					listToBePaginated.size());
		}
		// section
		if (paginationParams[2] > 0 && paginationParams[3] > 0) {
			if (paginationParams[2] >= listToBePaginated.size()) {
				paginatedList.clear();
			} else {
				if (paginationParams[3] >= listToBePaginated.size()) {
					paginationParams[3] = listToBePaginated.size();
				}
				// sublist toRange seems to be exclusive therefore +1
				LOGGER.log(Level.FINE, "Returning list from {0} to {1}",
						new Object[]{paginationParams[2] - 1,
								paginationParams[3]});
				paginatedList = listToBePaginated.subList(
						paginationParams[2] - 1, paginationParams[3]);
			}
		}
		return paginatedList;
	}

	@SuppressWarnings("unchecked")
	private void buildHATEOASLinks(List<T> retList, UriInfo uriInfo) {
		// Build HATEOAS link

		this.resetLinks();
		UriBuilder mainServiceURI = uriInfo.getAbsolutePathBuilder();

		ServiceRegistryLink selfLink = new ServiceRegistryLink();
		selfLink.setRelation(ServiceRegistryLink.SELF);
		selfLink.setTarget(mainServiceURI.build());

		this.addLink(selfLink);

		UriBuilder tempUriBuilder;

		for (T dE : (List<T>) retList) {
			ServiceRegistryLink itemContLink = new ServiceRegistryLink();
			itemContLink.setRelation(ServiceRegistryLink.ITEM);

			tempUriBuilder = mainServiceURI.clone();

			if (this.get().equals(ServiceRegistryEntry.class)) {
				itemContLink.setTarget(tempUriBuilder.path(
						"/" + ((ServiceRegistryEntry) dE).getServiceId())
						.build());
			}
			if (this.get().equals(Dependency.class)) {
				itemContLink.setTarget(tempUriBuilder.path(
						"/" + ((Dependency) dE).getDependencyId()).build());
			}
			if (this.get().equals(TechnicalServiceDescription.class)) {
				itemContLink.setTarget(tempUriBuilder.path(
						"/"
								+ ((TechnicalServiceDescription) dE)
										.getTechnicalDescriptionId()).build());
			}
			if (this.get().equals(ServiceInstance.class)) {
				itemContLink.setTarget(tempUriBuilder.path(
						"/" + ((ServiceInstance) dE).getServiceInstanceId())
						.build());
			}
			if (this.get().equals(Binding.class)) {
				itemContLink.setTarget(tempUriBuilder.path(
						"/" + ((Binding) dE).getBindingId()).build());
			}
			if (this.get().equals(UserFeedback.class)) {
				itemContLink.setTarget(tempUriBuilder.path(
						"/" + ((UserFeedback) dE).getUserFeedbackId()).build());
			}
			this.addLink(itemContLink);
		}
	}

	@SuppressWarnings("unchecked")
	public List<T> readListOfItems(String collectionId,
			String containedCollectionId,
			Set<Entry<String, List<String>>> queryParams, UriInfo uriInfo)
			throws DAOGeneralSystemFault, DAONotFoundFault, DAONotSavedFault,
			DAOUpdateFailedFault {
		return readListOfItems(collectionId, containedCollectionId,
				queryParams, uriInfo, null);
	}

	/**
	 * Reads and returns a list of the contained items
	 */
	@SuppressWarnings("unchecked")
	public List<T> readListOfItems(String collectionId,
			String containedCollectionId,
			Set<Entry<String, List<String>>> queryParams, UriInfo uriInfo,
			String userId) throws DAOGeneralSystemFault, DAONotFoundFault,
			DAONotSavedFault, DAOUpdateFailedFault {
		List<T> returnList = new ArrayList<T>(0);
		int[] paginationParams = new int[4];
		String[] fieldsParams = null;

		if (queryParams != null && queryParams.size() > 0) {
			paginationParams = this.parsePaginationParams(queryParams);
			fieldsParams = parseFilteringParams(queryParams);
		}
		if (this.get().equals(ServiceRegistryEntry.class)) {
			// HOX: Registry entry pagination done in DAO for greater
			// efficiency..maybe others should be done there too
			returnList = (List<T>) serviceRegistryDAO.findAll(queryParams,
					paginationParams[0], paginationParams[1],
					paginationParams[2], paginationParams[3], userId);

			if (fieldsParams != null) {
				returnList = doListFiltering(fieldsParams, returnList);
			}
		}
		if (this.get().equals(TechnicalServiceDescription.class)) {
			ServiceRegistryEntry sRegEntry = serviceRegistryDAO
					.findServiceRegistryEntry(collectionId);
			returnList = (List<T>) sRegEntry.getServiceDescription()
					.getTechnicalServiceDescription();
			returnList = doPagination(returnList, paginationParams);
		}
		if (this.get().equals(Dependency.class)) {
			ServiceRegistryEntry sRegEntry = serviceRegistryDAO
					.findServiceRegistryEntry(collectionId);
			returnList = (List<T>) sRegEntry.getServiceDescription()
					.getDependency();
			returnList = doPagination(returnList, paginationParams);
		}
		if (this.get().equals(ServiceInstance.class)) {
			ServiceRegistryEntry sRegEntry = serviceRegistryDAO
					.findServiceRegistryEntry(collectionId);
			returnList = (List<T>) sRegEntry.getServiceInstance();
		}
		if (this.get().equals(Binding.class)) {
			ServiceInstance servInst = serviceInstanceDAO.findServiceInstance(
					collectionId, containedCollectionId);
			returnList = (List<T>) servInst.getServiceAccessEndPoint()
					.getBinding();
		}
		if (this.get().equals(UserFeedback.class)) {
			ServiceRegistryEntry sRegEntry = serviceRegistryDAO
					.findServiceRegistryEntry(collectionId);
			returnList = (List<T>) sRegEntry.getServiceDescription()
					.getUserFeedback();
			returnList = doPagination(returnList, paginationParams);
		}

		buildHATEOASLinks(returnList, uriInfo);

		return returnList;
	}

	public Response convertExceptionToResponse(ServiceException ex, Exception e) {
		String exceptionReason = "";
		if (ex != null) {
			exceptionReason = ex.getExceptionReason();
		} else {
			exceptionReason = e.getMessage();
		}
		ResponseBuilder respB = null;
		if (e instanceof DAONotFoundFault) {
			respB = Response.status(Response.Status.NOT_FOUND)
					.entity(exceptionReason).type(MediaType.TEXT_PLAIN);
		}
		if (e instanceof DAOUpdateFailedFault) {
			respB = Response.status(Response.Status.NOT_ACCEPTABLE)
					.entity(exceptionReason).type(MediaType.TEXT_PLAIN);
		}
		if (e instanceof DAONotSavedFault) {
			respB = Response.status(Response.Status.BAD_REQUEST)
					.entity(exceptionReason).type(MediaType.TEXT_PLAIN);
		}
		if (e instanceof DAOGeneralSystemFault) {
			respB = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(exceptionReason).type(MediaType.TEXT_PLAIN);
		}
		if (respB == null) {
			respB = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(exceptionReason).type(MediaType.TEXT_PLAIN);
		}
		if (ex != null) {
			LOGGER.log(
					Level.FINE,
					ex.getClass().getName()
							+ " - Exception reason: {0}, Exception code: {1}",
					new Object[]{ex.getExceptionReason(), ex.getExceptionCode()});
		} else {
			LOGGER.log(Level.FINE, "ItemResource: {0} exception {1}",
					new Object[]{this.getClass().getName(), e.getStackTrace()});
		}
		return respB.build();
	}
}