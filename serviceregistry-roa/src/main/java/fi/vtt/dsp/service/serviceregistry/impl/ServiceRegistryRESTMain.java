package fi.vtt.dsp.service.serviceregistry.impl;

import fi.vtt.dsp.service.serviceregistry.impl.roa.ServiceRegistrationROAInterface;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.cxf.annotations.GZIP;

/**
 * Main entry point for all resources for the service registry REST
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:02
 */

@Path("/")
@GZIP
public class ServiceRegistryRESTMain extends ServiceRegistrationROAInterface {
	/**
	 * Returns main page of the service with helpful links
	 */
	@GET
	@Produces({MediaType.TEXT_HTML})
	public String getMainInfoPage(@Context UriInfo uriinfo) {

		UriBuilder mainServiceURI = uriinfo.getAbsolutePathBuilder();
		mainServiceURI.path("/resourcedirectory/v1/serviceregistrations");
		String mainLink = "<a href=\"" + mainServiceURI.build().toString()
				+ "\">" + mainServiceURI.build().toString() + "</a>";

		UriBuilder wadlURI = uriinfo.getAbsolutePathBuilder();
		wadlURI.queryParam("_wadl");
		String wadlLink = "<a href=\"" + wadlURI.build().toString() + "\">"
				+ wadlURI.build().toString() + "</a>";

		String mainInfoPage = "<!DOCTYPE html><html><body><h1>Digital Service Registry</h1><p>Main Service Registry resource supporting application/json and application/xml MIME types is available at </p><p>"
				+ mainLink
				+ "</p><p>See endpoint WADL for more information</p><p>"
				+ wadlLink + "</p></body>.</html>";

		return mainInfoPage;
	}
}
