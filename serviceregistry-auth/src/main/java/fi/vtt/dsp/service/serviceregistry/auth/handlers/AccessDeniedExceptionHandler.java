package fi.vtt.dsp.service.serviceregistry.auth.handlers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.security.access.AccessDeniedException;

public class AccessDeniedExceptionHandler
		implements
			ExceptionMapper<AccessDeniedException> {

	@Override
	public Response toResponse(AccessDeniedException e) {

		return Response.status(Response.Status.FORBIDDEN)
				.entity("No authorization to access the resource").build();
	}

}
