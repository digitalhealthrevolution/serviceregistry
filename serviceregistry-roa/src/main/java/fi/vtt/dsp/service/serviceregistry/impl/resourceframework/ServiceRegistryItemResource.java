package fi.vtt.dsp.service.serviceregistry.impl.resourceframework;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.data.ServiceDataDescription;
import fi.vtt.dsp.service.serviceregistry.common.description.Dependency;
import fi.vtt.dsp.service.serviceregistry.common.description.HumanReadableDescription;
import fi.vtt.dsp.service.serviceregistry.common.description.TechnicalServiceDescription;
import fi.vtt.dsp.service.serviceregistry.common.description.ServiceDescription;
import fi.vtt.dsp.service.serviceregistry.common.description.UserFeedback;
import fi.vtt.dsp.service.serviceregistry.common.instance.Availability;
import fi.vtt.dsp.service.serviceregistry.common.instance.AvailabilityRequestEndPoint;
import fi.vtt.dsp.service.serviceregistry.common.instance.BindingRequestEndPoint;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint;
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
import fi.vtt.dsp.service.serviceregistry.impl.util.ExceptionUtil;
import fi.vtt.dsp.serviceframework.common.Binding;
import fi.vtt.dsp.serviceframework.exceptions.*;
import fi.vtt.dsp.serviceframework.resourceframework.ItemResource;
import fi.vtt.dsp.serviceframework.resourceframework.Link;
import fi.vtt.dsp.serviceframework.resourceframework.ResourceRepresentation;

/**
 * Individual item resource with URI and content manipulation methods
 *
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:01
 */
public abstract class ServiceRegistryItemResource<T> extends ItemResource {

    private static final Logger LOGGER = Logger
        .getLogger(ServiceRegistryItemResource.class.getName());

    private ResourceRepresentation mResourceRepresentation;
    private ServiceRegistryDAO serviceRegistryDAO;
    private ServiceDescriptionDAO serviceDescriptionDAO;
    private ServiceInstanceDAO serviceInstanceDAO;
    private Class<T> t;

    public ServiceRegistryItemResource(Class<T> resourceType) {
        this.t = resourceType;
        this.setLink(new ArrayList<Link>());
    }

    private void setDAOs() {
        try {
            if (serviceRegistryDAO == null) {
                this.serviceRegistryDAO = new MongoDBServiceRegistryDAO();
            }
            if (serviceDescriptionDAO == null) {
                this.serviceDescriptionDAO = new MongoDBServiceDescriptionDAO();
            }
            if (serviceInstanceDAO == null) {
                this.serviceInstanceDAO = new MongoDBServiceInstanceDAO();
            }
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

    // Link as RFC 5988 Web Linking draft-nottingham-http-link-header
    public String getLinkAsNottinghamHTTPString(Link link) {
        return "<" + link.getTarget() + ">; rel=\"" + link.getRelation() + "\"";
    }

    public List<Link> getLinks() {
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

    private void buildHATEOASLinks(T retItem, String collectionId,
        String itemId, UriInfo uriInfo) {
        // Build HATEOAS link

        if (retItem != null) {
            this.resetLinks();
            UriBuilder mainServiceURI = uriInfo.getAbsolutePathBuilder();

            ServiceRegistryLink selfLink = new ServiceRegistryLink();
            selfLink.setRelation(ServiceRegistryLink.SELF);
            selfLink.setTarget(mainServiceURI.build());

            this.addLink(selfLink);

            // Single items
            if (this.get().equals(ServiceRegistryEntry.class)) {
                UriBuilder containerURIB = mainServiceURI.clone();
                // embedded container
                ServiceRegistryLink embeddedContLink = new ServiceRegistryLink();
                embeddedContLink
                    .setRelation(ServiceRegistryLink.EMBEDEDCONTAINER);
                embeddedContLink.setTarget(containerURIB.path(
                    "/serviceinstances").build());
                this.addLink(embeddedContLink);

                // items
                UriBuilder descItemURIB = mainServiceURI.clone();
                if (((ServiceRegistryEntry) retItem).getServiceDescription() != null) {
                    ServiceRegistryLink descItemContLink = new ServiceRegistryLink();
                    descItemContLink.setRelation(ServiceRegistryLink.ITEM);
                    descItemContLink.setTarget(descItemURIB.path(
                        "/servicedescription").build());
                    this.addLink(descItemContLink);
                }

            }

            if (this.get().equals(ServiceDescription.class)) {
                // embedded container
                UriBuilder servDescItemURIB = mainServiceURI.clone();
                ServiceRegistryLink techDescEmbeddedContLink = new ServiceRegistryLink();
                techDescEmbeddedContLink
                    .setRelation(ServiceRegistryLink.EMBEDEDCONTAINER);
                techDescEmbeddedContLink.setTarget(servDescItemURIB.path(
                    "/technicaldescriptions").build());
                this.addLink(techDescEmbeddedContLink);

                servDescItemURIB = mainServiceURI.clone();
                ServiceRegistryLink dataDescEmbeddedContLink = new ServiceRegistryLink();
                dataDescEmbeddedContLink
                    .setRelation(ServiceRegistryLink.EMBEDEDCONTAINER);
                dataDescEmbeddedContLink.setTarget(servDescItemURIB.path(
                    "/datadescriptions").build());
                this.addLink(dataDescEmbeddedContLink);

                servDescItemURIB = mainServiceURI.clone();
                ServiceRegistryLink depEmbeddedContLink = new ServiceRegistryLink();
                depEmbeddedContLink
                    .setRelation(ServiceRegistryLink.EMBEDEDCONTAINER);
                depEmbeddedContLink.setTarget(servDescItemURIB.path(
                    "/dependencies").build());
                this.addLink(depEmbeddedContLink);

                servDescItemURIB = mainServiceURI.clone();
                ServiceRegistryLink uFBEmbeddedContLink = new ServiceRegistryLink();
                uFBEmbeddedContLink
                    .setRelation(ServiceRegistryLink.EMBEDEDCONTAINER);
                uFBEmbeddedContLink.setTarget(servDescItemURIB.path(
                    "/userfeedbacks").build());
                this.addLink(uFBEmbeddedContLink);
            }

            if (this.get().equals(HumanReadableDescription.class)) {

            }

            // Items in collections
            if (this.get().equals(Binding.class)) {
                // index
                ServiceRegistryLink indexLink = new ServiceRegistryLink();
                indexLink.setRelation(ServiceRegistryLink.INDEX);
                String tempURIString = mainServiceURI.toString();
                tempURIString.replaceAll(itemId, "");
                indexLink.setTarget(mainServiceURI.replacePath(tempURIString)
                    .build());
                this.addLink(indexLink);
            }

            if (this.get().equals(ServiceDataDescription.class)) {
                // index
                ServiceRegistryLink indexLink = new ServiceRegistryLink();
                indexLink.setRelation(ServiceRegistryLink.INDEX);
                String tempURIString = mainServiceURI.toString();
                tempURIString.replaceAll(itemId, "");
                indexLink.setTarget(mainServiceURI.replacePath(tempURIString)
                    .build());
                this.addLink(indexLink);
            }
            if (this.get().equals(TechnicalServiceDescription.class)) {
                // index
                ServiceRegistryLink indexLink = new ServiceRegistryLink();
                indexLink.setRelation(ServiceRegistryLink.INDEX);
                String tempURIString = mainServiceURI.toString();
                tempURIString.replaceAll(itemId, "");
                indexLink.setTarget(mainServiceURI.replacePath(tempURIString)
                    .build());
                this.addLink(indexLink);
            }

            if (this.get().equals(Dependency.class)) {
                // index
                ServiceRegistryLink indexLink = new ServiceRegistryLink();
                indexLink.setRelation(ServiceRegistryLink.INDEX);
                String tempURIString = mainServiceURI.toString();
                tempURIString.replaceAll(itemId, "");
                indexLink.setTarget(mainServiceURI.replacePath(tempURIString)
                    .build());
                this.addLink(indexLink);
            }

            if (this.get().equals(ServiceInstance.class)) {
                // index
                ServiceRegistryLink indexLink = new ServiceRegistryLink();
                indexLink.setRelation(ServiceRegistryLink.INDEX);
                String tempURIString = mainServiceURI.toString();
                tempURIString.replaceAll(itemId, "");
                indexLink.setTarget(mainServiceURI.replacePath(tempURIString)
                    .build());
                this.addLink(indexLink);

                if (((ServiceInstance) retItem).getServiceAccessEndPoint() != null) {
                    UriBuilder servInstanceItemURIB = mainServiceURI.clone();
                    ServiceRegistryLink servAccessEPItemContLink = new ServiceRegistryLink();
                    servAccessEPItemContLink
                        .setRelation(ServiceRegistryLink.ITEM);
                    servAccessEPItemContLink.setTarget(servInstanceItemURIB
                        .path("/serviceaccessendpoint").build());
                    this.addLink(servAccessEPItemContLink);
                }
                if (((ServiceInstance) retItem)
                    .getAvailabilityRequestEndPoint() != null) {
                    UriBuilder servInstanceItemURIB = mainServiceURI.clone();
                    ServiceRegistryLink servAccessEPItemContLink = new ServiceRegistryLink();
                    servAccessEPItemContLink
                        .setRelation(ServiceRegistryLink.ITEM);
                    servAccessEPItemContLink.setTarget(servInstanceItemURIB
                        .path("/availabilityendpoint").build());
                    this.addLink(servAccessEPItemContLink);

                }
                if (((ServiceInstance) retItem).getBindingRequestEndPoint() != null) {
                    UriBuilder servInstanceItemURIB = mainServiceURI.clone();
                    ServiceRegistryLink servAccessEPItemContLink = new ServiceRegistryLink();
                    servAccessEPItemContLink
                        .setRelation(ServiceRegistryLink.ITEM);
                    servAccessEPItemContLink.setTarget(servInstanceItemURIB
                        .path("/bindingendpoint").build());
                    this.addLink(servAccessEPItemContLink);

                }

            }

            if (this.get().equals(AvailabilityRequestEndPoint.class)) {

            }
            if (this.get().equals(BindingRequestEndPoint.class)) {

            }
            if (this.get().equals(ServiceAccessEndPoint.class)) {
                ServiceRegistryLink bindingsEmbeddedContLink = new ServiceRegistryLink();
                bindingsEmbeddedContLink
                    .setRelation(ServiceRegistryLink.EMBEDEDCONTAINER);
                bindingsEmbeddedContLink.setTarget(mainServiceURI.path(
                    "/bindings").build());
                this.addLink(bindingsEmbeddedContLink);

            }
            if (this.get().equals(Availability.class)) {

            }
            if (this.get().equals(UserFeedback.class)) {
                // index
                ServiceRegistryLink indexLink = new ServiceRegistryLink();
                indexLink.setRelation(ServiceRegistryLink.INDEX);
                String tempURIString = mainServiceURI.toString();
                tempURIString.replaceAll(itemId, "");
                indexLink.setTarget(mainServiceURI.replacePath(tempURIString)
                    .build());
                this.addLink(indexLink);
            }
        }
    }

    /**
     * Returns individual item representation of this resource
     *
     * @throws Exception
     */
    public T readItem(String itemId, UriInfo uriInfo)
        throws DAOGeneralSystemFault, DAONotFoundFault, DAONotSavedFault,
        DAOUpdateFailedFault {
        setDAOs();
        ServiceRegistryEntry sRegEntry;
        sRegEntry = serviceRegistryDAO.findServiceRegistryEntry(itemId);
        T retObj = null;

        if (sRegEntry != null) {
            if (this.get().equals(ServiceRegistryEntry.class)) {
                retObj = this.t.cast(sRegEntry);
            }
            if (this.get().equals(ServiceDescription.class)) {
                retObj = this.t.cast(sRegEntry.getServiceDescription());
            }
            if (this.get().equals(HumanReadableDescription.class)) {
                retObj = this.t.cast(sRegEntry.getServiceDescription()
                    .getHumanReadableDescription());
            }
        }
        return retObj;
    }

    /**
     * Returns individual item representation of this from the resource collection
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public T readItem(String collectionId, String itemId, UriInfo uriInfo)
        throws DAOGeneralSystemFault, DAONotFoundFault, DAONotSavedFault,
        DAOUpdateFailedFault {
        setDAOs();
        T retObj = null;

        if (this.get().equals(TechnicalServiceDescription.class)) {
            retObj = (T) serviceDescriptionDAO
                .findTechnicalServiceDescription(collectionId, itemId);
        }
        if (this.get().equals(ServiceDataDescription.class)) {
            retObj = (T) serviceDescriptionDAO
                .findDataDescription(collectionId, itemId);
        }
        if (this.get().equals(Dependency.class)) {
            retObj = (T) serviceDescriptionDAO.findDependency(collectionId,
                itemId);
        }
        if (this.get().equals(ServiceInstance.class)) {
            retObj = (T) serviceInstanceDAO.findServiceInstance(
                collectionId, itemId);
        }
        if (this.get().equals(AvailabilityRequestEndPoint.class)) {
            retObj = (T) serviceInstanceDAO
                .findAvailabilityRequestEndPoint(collectionId, itemId);
        }
        if (this.get().equals(BindingRequestEndPoint.class)) {
            retObj = (T) serviceInstanceDAO.findBindingRequestEndPoint(
                collectionId, itemId);
        }
        if (this.get().equals(ServiceAccessEndPoint.class)) {
            retObj = (T) serviceInstanceDAO.findServiceAccessEndPoint(
                collectionId, itemId);
        }
        if (this.get().equals(Availability.class)) {
            retObj = (T) serviceInstanceDAO.findAvailability(collectionId,
                itemId);
        }
        if (this.get().equals(UserFeedback.class)) {
            retObj = (T) serviceDescriptionDAO.findUserFeedback(
                collectionId, itemId);
        }

        return retObj;
    }

    /**
     * Returns individual item representation of this from the resource collection
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public T readItemFromContainerInContainer(String collectionId,
        String collectionInCollectionId, String itemId, UriInfo uriInfo)
        throws DAOGeneralSystemFault, DAONotFoundFault, DAONotSavedFault,
        DAOUpdateFailedFault {
        setDAOs();
        T retObj = null;
        if (this.get().equals(Binding.class)) {
            retObj = (T) serviceInstanceDAO.findBinding(collectionId,
                collectionInCollectionId, itemId);
        }
        return retObj;
    }

    /**
     * Updates this item resource with the provided content
     *
     * @param updatedItemRepresentation Updated item presentation of the resource
     */
    public void updateItem(String itemId, T updatedItemRepresentation)
        throws DAOGeneralSystemFault, DAONotFoundFault, DAONotSavedFault,
        DAOUpdateFailedFault {
        setDAOs();
        if (this.get().equals(ServiceRegistryEntry.class)) {
            serviceRegistryDAO.updateServiceRegistryEntry(itemId,
                (ServiceRegistryEntry) updatedItemRepresentation);
        }
        if (this.get().equals(ServiceDescription.class)) {
            ServiceRegistryEntry sRegEntry = serviceRegistryDAO
                .findServiceRegistryEntry(itemId);
            sRegEntry
                .setServiceDescription((ServiceDescription) updatedItemRepresentation);
            serviceRegistryDAO.updateServiceRegistryEntry(itemId, sRegEntry);
        }
        if (this.get().equals(HumanReadableDescription.class)) {
            ServiceRegistryEntry sRegEntry = serviceRegistryDAO
                .findServiceRegistryEntry(itemId);
            sRegEntry.getServiceDescription().setHumanReadableDescription(
                (HumanReadableDescription) updatedItemRepresentation);
            serviceRegistryDAO.updateServiceRegistryEntry(itemId, sRegEntry);
        }
    }

    /**
     * Updates this contained item resource with the provided content
     *
     * @param updatedItemRepresentation Updated item presentation of the resource
     */
    public void updateItem(String collectionId, String itemId,
        T updatedItemRepresentation) throws DAOGeneralSystemFault,
        DAONotFoundFault, DAONotSavedFault, DAOUpdateFailedFault {
        setDAOs();
        if (this.get().equals(TechnicalServiceDescription.class)) {
            serviceDescriptionDAO.updateTechnicalServiceDescription(
                collectionId, itemId,
                (TechnicalServiceDescription) updatedItemRepresentation);
        }
        if (this.get().equals(ServiceDataDescription.class)) {
            serviceDescriptionDAO.updateDataDescription(collectionId, itemId,
                (ServiceDataDescription) updatedItemRepresentation);
        }
        if (this.get().equals(Dependency.class)) {
            serviceDescriptionDAO.updateDependency(collectionId, itemId,
                (Dependency) updatedItemRepresentation);
        }
        if (this.get().equals(ServiceInstance.class)) {
            serviceInstanceDAO.updateServiceInstance(collectionId, itemId,
                (ServiceInstance) updatedItemRepresentation);
        }
        if (this.get().equals(AvailabilityRequestEndPoint.class)) {
            serviceInstanceDAO.updateAvailabilityRequestEndPoint(collectionId,
                itemId,
                (AvailabilityRequestEndPoint) updatedItemRepresentation);
        }
        if (this.get().equals(BindingRequestEndPoint.class)) {
            serviceInstanceDAO.updateBindingRequestEndPoint(collectionId,
                itemId, (BindingRequestEndPoint) updatedItemRepresentation);
        }
        if (this.get().equals(ServiceAccessEndPoint.class)) {
            serviceInstanceDAO.updateServiceAccessEndPoint(collectionId,
                itemId, (ServiceAccessEndPoint) updatedItemRepresentation);
        }
        if (this.get().equals(Availability.class)) {
            serviceInstanceDAO.updateAvailability(collectionId, itemId,
                (Availability) updatedItemRepresentation);
        }
        if (this.get().equals(UserFeedback.class)) {
            serviceDescriptionDAO.updateUserFeedback(collectionId, itemId, (UserFeedback) updatedItemRepresentation);
        }
    }

    /**
     * Updates this contained item resource with the provided content
     *
     * @param collectionId Main container identification
     * @param collectionInCollectionId Container located in container
     * @param itemId Item identifier
     * @param updatedItemRepresentation Updated item presentation of the resource
     */
    public void updateItemInContainerInContainer(String collectionId,
        String collectionInCollectionId, String itemId,
        T updatedItemRepresentation) throws DAOGeneralSystemFault,
        DAONotFoundFault, DAONotSavedFault, DAOUpdateFailedFault {
        setDAOs();
        if (this.get().equals(Binding.class)) {
            serviceInstanceDAO.updateBinding(collectionId,
                collectionInCollectionId, itemId,
                (Binding) updatedItemRepresentation);
        }
    }

    /**
     * Removes this item resource
     */
    public void removeItem(String itemId) throws DAOGeneralSystemFault,
        DAONotFoundFault, DAONotSavedFault, DAOUpdateFailedFault {
        setDAOs();
        if (this.get().equals(ServiceRegistryEntry.class)) {
            serviceRegistryDAO.deleteServiceRegistryEntry(itemId);
        }
        if (this.get().equals(ServiceDescription.class)) {
            ServiceRegistryEntry sRegEntry = serviceRegistryDAO
                .findServiceRegistryEntry(itemId);
            sRegEntry.setServiceDescription(null);
            serviceRegistryDAO.updateServiceRegistryEntry(itemId, sRegEntry);
        }
        if (this.get().equals(HumanReadableDescription.class)) {
            ServiceRegistryEntry sRegEntry = serviceRegistryDAO
                .findServiceRegistryEntry(itemId);
            sRegEntry.getServiceDescription().setHumanReadableDescription(null);
            serviceRegistryDAO.updateServiceRegistryEntry(itemId, sRegEntry);
        }
    }

    public void removeItem(String collectionId, String itemId)
        throws DAOGeneralSystemFault, DAONotFoundFault, DAONotSavedFault,
        DAOUpdateFailedFault {
        setDAOs();
        if (this.get().equals(TechnicalServiceDescription.class)) {
            // Corresponding ServiceInstance must be removed too
            serviceDescriptionDAO.deleteTechnicalServiceDescription(
                collectionId, itemId);
        }
        if (this.get().equals(ServiceDataDescription.class)) {
            serviceDescriptionDAO.deleteDataDescription(collectionId, itemId);
        }
        if (this.get().equals(Dependency.class)) {
            serviceDescriptionDAO.deleteDependency(collectionId, itemId);
        }
        if (this.get().equals(ServiceInstance.class)) {
            serviceInstanceDAO.deleteServiceInstance(collectionId, itemId);
        }
        if (this.get().equals(AvailabilityRequestEndPoint.class)) {
            serviceInstanceDAO.deleteAvailabilityRequestEndPoint(collectionId,
                itemId);
        }
        if (this.get().equals(BindingRequestEndPoint.class)) {
            serviceInstanceDAO.deleteBindingRequestEndPoint(collectionId,
                itemId);
        }
        if (this.get().equals(ServiceAccessEndPoint.class)) {
            serviceInstanceDAO
                .deleteServiceAccessEndPoint(collectionId, itemId);
        }
        if (this.get().equals(Availability.class)) {
            serviceInstanceDAO.deleteAvailability(collectionId, itemId);
        }
        if (this.get().equals(UserFeedback.class)) {
            serviceDescriptionDAO.deleteUserFeedback(collectionId, itemId);
        }
    }

    public void removeItem(String collectionId,
        String collectionInCollectionId, String itemId)
        throws DAOGeneralSystemFault, DAONotFoundFault, DAONotSavedFault,
        DAOUpdateFailedFault {
        setDAOs();
        if (this.get().equals(Binding.class)) {
            serviceInstanceDAO.deleteBinding(collectionId,
                collectionInCollectionId, itemId);
        }
    }

    public Response convertExceptionToResponse(ServiceException ex, Exception e) {
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
        return ExceptionUtil.convertExceptionToResponse(ex, e);
    }
}
