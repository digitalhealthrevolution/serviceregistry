package fi.vtt.dsp.service.serviceregistry.impl.roa;

import fi.vtt.dsp.service.serviceregistry.common.data.ServiceDataDescription;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotSavedFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOUpdateFailedFault;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemResource;
import fi.vtt.dsp.service.serviceregistry.impl.util.ExceptionUtil;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author Juhani Laitakari
 */
@Path("resourcedirectory/v1/serviceregistrations/{serviceid}/servicedescription/datadescriptions/{datadescriptionid}")
public class ServiceDataDescriptionResource extends ServiceRegistryItemResource<ServiceDataDescription> {

    @Context
    private UriInfo uriInfo;

    public ServiceDataDescriptionResource() {
        super(ServiceDataDescription.class);
    }

    @GET
    @Produces("application/json")
    @PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canGet(authentication)")
    public Response get(
        @PathParam("serviceid") String serviceId,
        @PathParam("datadescriptionid") String dataDescId
    ) {

        Response resp;
        final String expCode = "10003";
        try {
            ServiceDataDescription dataDesc = this.readItem(serviceId, dataDescId, uriInfo);
            resp = Response.ok(dataDesc).build();
        } catch (DAONotFoundFault e) {
            resp = ExceptionUtil.getNotExistsResponse(e, expCode);
        } catch (DAOGeneralSystemFault | DAONotSavedFault | DAOUpdateFailedFault e) {
            resp = ExceptionUtil.getErrorResponse(e, expCode);
        }
        return resp;
    }

    @PUT
    @Consumes("application/json")
    @PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canUpdate(authentication)")
    public Response update(
        @PathParam("serviceid") String serviceId,
        @PathParam("datadescriptionid") String dataDescId,
        ServiceDataDescription updateData
    ) {

        Response resp;
        final String expCode = "10002";
        try {
            this.updateItem(serviceId, dataDescId, updateData);
            resp = Response.ok().build();
        } catch (DAONotFoundFault e) {
            resp = ExceptionUtil.getNotExistsResponse(e, expCode);
        } catch (DAOGeneralSystemFault | DAONotSavedFault | DAOUpdateFailedFault e) {
            resp = ExceptionUtil.getErrorResponse(e, expCode);
        }
        return resp;
    }

    @DELETE
    @PreAuthorize("@serviceRegistrationAuth.set(#serviceId).canDelete(authentication)")
    public Response delete(
        @PathParam("serviceid") String serviceId,
        @PathParam("datadescriptionid") String dataDescId
    ) {

        Response resp;
        final String expCode = "10003";
        try {
            this.removeItem(serviceId, dataDescId);
            resp = Response.ok().build();
        } catch (DAONotFoundFault e) {
            resp = ExceptionUtil.getNotExistsResponse(e, expCode);
        } catch (DAOGeneralSystemFault | DAONotSavedFault | DAOUpdateFailedFault e) {
            resp = ExceptionUtil.getErrorResponse(e, expCode);
        }
        return resp;
    }
}
