package fi.vtt.dsp.service.serviceregistry.impl.roa;

import fi.vtt.dsp.service.serviceregistry.common.data.ServiceDataDescription;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotSavedFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOUpdateFailedFault;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemCollectionResource;
import fi.vtt.dsp.service.serviceregistry.impl.util.ExceptionUtil;
import java.net.URI;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author Juhani Laitakari
 */
@Path("resourcedirectory/v1/serviceregistrations/{serviceid}/servicedescription/datadescriptions")
public class ServiceDataDescriptionsResource extends ServiceRegistryItemCollectionResource<ServiceDataDescription> {

    @Context
    private UriInfo uriInfo;

    public ServiceDataDescriptionsResource() {
        super(ServiceDataDescription.class);
    }

    @GET
    @Produces("application/json")
    @PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canGet(authentication)")
    public Response list(@PathParam("serviceid") String serviceId) {

        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

        Response resp;
        final String expCode = "10004";
        try {
            List<ServiceDataDescription> list = this.readListOfItems(serviceId, null, queryParams.entrySet(), uriInfo);
            resp = Response.ok(list).build();
        } catch (DAONotFoundFault e) {
            resp = ExceptionUtil.getNotExistsResponse(e, expCode);
        } catch (DAOGeneralSystemFault | DAONotSavedFault | DAOUpdateFailedFault e) {
            resp = ExceptionUtil.getErrorResponse(e, expCode);
        }
        return resp;
    }

    @POST
    @Consumes("application/json")
    @PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canUpdate(authentication)")
    public Response create(
        @PathParam("serviceid") String serviceId,
        ServiceDataDescription newData
    ) {

        Response resp;
        final String expCode = "10005";
        try {
            String id = this.createItem(serviceId, newData);
            URI createdUri = uriInfo
                .getAbsolutePathBuilder()
                .path(id)
                .build();
            resp = Response.created(createdUri).build();
        } catch (DAONotFoundFault e) {
            resp = ExceptionUtil.getNotExistsResponse(e, expCode);
        } catch (DAOGeneralSystemFault | DAONotSavedFault | DAOUpdateFailedFault e) {
            resp = ExceptionUtil.getErrorResponse(e, expCode);
        }
        return resp;
    }

}
