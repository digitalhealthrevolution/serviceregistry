package fi.vtt.dsp.service.serviceregistry.impl.roa;

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryLogEntry;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotSavedFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOUpdateFailedFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.KafkaLogDAO;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemCollectionResource;
import java.util.List;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.springframework.security.access.prepost.PreAuthorize;

@Path("/resourcedirectory/v1/logs")
public class LogResource extends ServiceRegistryItemCollectionResource<ServiceRegistryLogEntry> {
    private static final Logger LOGGER = Logger.getLogger(LogResource.class.getName());
    
    public LogResource() {
		super(ServiceRegistryLogEntry.class);
	}
    
    @POST
	@Produces("application/json")
    @PreAuthorize("@logResourceAuth.set(#logItem).canInsert(authentication)")
	public Response insertLogItem(ServiceRegistryLogEntry logItem) {
        Response response;

		try {
            KafkaLogDAO kafkaLogDAO = new KafkaLogDAO();
            kafkaLogDAO.insertLogItem(logItem);
            response = Response.ok().build();
		} 
        catch (DAOGeneralSystemFault | DAOUpdateFailedFault | DAONotSavedFault | DAONotFoundFault e) {
            response = Response.serverError().build();
		}

		return response;
    }
    
    @GET
	@Produces("application/json")
    @Path("/service/{serviceid}/instance/{instanceid}")
    @PreAuthorize("@logResourceAuth.set(#serviceId).canGet(authentication)")
	public Response getLogsForServiceInstance(@PathParam("serviceid") String serviceId, @PathParam("instanceid") String instanceId) {
        Response response;

		try {
            KafkaLogDAO kafkaLogDAO = new KafkaLogDAO();
            List<ServiceRegistryLogEntry> logItems = kafkaLogDAO.getAllLogsForServiceInstance(serviceId, instanceId);
            response = Response.ok(logItems.toArray(new ServiceRegistryLogEntry[logItems.size()])).build();
		} 
        catch (Exception e) {
            response = Response.serverError().build();
		}

		return response;
    }
    
    @GET
	@Produces("application/json")
    @Path("/service/{serviceid}/instance/{instanceid}/timestart/{timestart}/timeend/{timeend}")
    @PreAuthorize("@logResourceAuth.set(#serviceId).canGet(authentication)")
	public Response getLogsForServiceInstance(@PathParam("serviceid") String serviceId, @PathParam("instanceid") String instanceId, 
            @PathParam("timestart") String timeStart, @PathParam("timeend") String timeEnd) {
        Response response;

		try {
            KafkaLogDAO kafkaLogDAO = new KafkaLogDAO();
            List<ServiceRegistryLogEntry> logItems = kafkaLogDAO.getAllLogsForServiceInstanceForTimeInterval(serviceId, instanceId, timeStart, timeEnd);
            response = Response.ok(logItems.toArray(new ServiceRegistryLogEntry[logItems.size()])).build();
		} 
        catch (Exception e) {
            response = Response.serverError().build();
		}

		return response;
    }
}
