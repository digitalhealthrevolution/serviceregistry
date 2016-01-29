package fi.vtt.dsp.service.serviceregistry.impl.util;

import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationDoesNotExistException;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationException;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotSavedFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOUpdateFailedFault;
import fi.vtt.dsp.serviceframework.exceptions.ServiceException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Juhani Laitakari
 */
public class ExceptionUtil {

    public static Response getNotExistsResponse(Exception e, String code) {
        ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
        ex.setExceptionReason(e.getMessage());
        ex.setExceptionCode(code);
        return convertExceptionToResponse(ex, e);
    }

    public static Response getErrorResponse(Exception e, String code) {
        ServiceRegistrationException ex = new ServiceRegistrationException();
        ex.setExceptionReason(e.getMessage());
        ex.setExceptionCode(code);
        return convertExceptionToResponse(ex, e);
    }

    public static Response convertExceptionToResponse(ServiceException ex, Exception e) {
        String exceptionReason;
        if (ex != null) {
            exceptionReason = ex.getExceptionReason();
        } else {
            exceptionReason = e.getMessage();
        }
        Response.ResponseBuilder respB = null;
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
        return respB.build();
    }
}
