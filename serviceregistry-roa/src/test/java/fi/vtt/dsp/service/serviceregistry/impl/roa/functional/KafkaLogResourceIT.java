package fi.vtt.dsp.service.serviceregistry.impl.roa.functional;

import com.mongodb.DBCollection;
import fi.vtt.dsp.service.serviceregistry.TestDb;
import fi.vtt.dsp.service.serviceregistry.auth.token.AccessToken;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryLogEntry;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBConnection;
import fi.vtt.dsp.service.serviceregistry.impl.roa.BaseAuthIT;
import fi.vtt.dsp.service.serviceregistry.impl.roa.TestAccessTokenFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class KafkaLogResourceIT extends BaseAuthIT {
    private static final Logger LOGGER = Logger.getLogger(KafkaLogResourceIT.class.getName());
    long timeStampSent;
    
    @After    
    public void clearDatabase() {
        MongoDBConnection mongoDBConnection = TestDb.getDbConnection("serviceregistry.kafkalog");
        
        try {
            DBCollection dBCollection = mongoDBConnection.getDBCollection();
            dBCollection.drop();
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot clear kafka-log database", e);
        }
    }
    
    @Test
    public void testInsertAndGets() {
        AccessToken serviceAccessToken = TestAccessTokenFactory.getRegisteredAgentAT(serviceAgent);
        WebClient webClient = setupJSONClient("/logs", serviceAccessToken);
        ServiceRegistryLogEntry logItem = new ServiceRegistryLogEntry();
        timeStampSent = System.currentTimeMillis();
        logItem.setTimeStamp(Long.toString(timeStampSent));
        logItem.setHostingServiceId(serviceAgent.getServiceId());
        logItem.setHostingInstanceId("123111");
        logItem.setRequestingServiceId("123");
        logItem.setRequestingInstanceId("123222");
        logItem.setTimeSpent("100");
        
        LOGGER.log(Level.FINE, "Posting log-item");
        Response response = webClient.post(logItem);
        
        LOGGER.log(Level.FINE, "Posted");
        
        Assert.assertEquals(200, response.getStatus());        
        
        webClient = setupJSONClient("/logs/service/" + logItem.getHostingServiceId() + "/instance/" + logItem.getHostingInstanceId(), serviceAccessToken);
        response = webClient.get();
        String responseBody = response.readEntity(String.class);
        ServiceRegistryLogEntry[] logItems;
        
        Assert.assertEquals(200, response.getStatus());
        
        try {
            logItems = MAPPER.readValue(responseBody, ServiceRegistryLogEntry[].class);
            Assert.assertEquals(1, logItems.length);
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to read log-items", e);
            Assert.fail();
        }
        
        
        webClient = setupJSONClient("/logs/service/" + logItem.getHostingServiceId() + "/instance/" + logItem.getHostingInstanceId() +
                "/timestart/" + Long.toString(timeStampSent - 100) + "/timeend/" + Long.toString(timeStampSent + 100), serviceAccessToken);
        response = webClient.get();
        responseBody = response.readEntity(String.class);

        Assert.assertEquals(200, response.getStatus());
        
        try {
            logItems = MAPPER.readValue(responseBody, ServiceRegistryLogEntry[].class);
            Assert.assertEquals(1, logItems.length);
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to read log-items", e);
            Assert.fail();
        }
        
        webClient = setupJSONClient("/logs/service/" + logItem.getHostingServiceId() + "/instance/" + logItem.getHostingInstanceId() +
                "/timestart/" + Long.toString(timeStampSent + 100) + "/timeend/" + Long.toString(timeStampSent + 200), serviceAccessToken);
        response = webClient.get();
        responseBody = response.readEntity(String.class);

        Assert.assertEquals(200, response.getStatus());
        
        try {
            logItems = MAPPER.readValue(responseBody, ServiceRegistryLogEntry[].class);
            Assert.assertEquals(0, logItems.length);
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to read log-items", e);
            Assert.fail();
        }
        
        
        // @Path("/service/{serviceid}/instance/{instanceid}")
                
                
        // @Path("/service/{serviceid}/instance/{instanceid}/timestart/{timestart}/timeend/{timeend}")
        /*
        client = setUpJSONClient("/resourcedirectory/v1/usergroups");
		mapper = new ObjectMapper();

		response = client.get();
		String responseBody = response.readEntity(String.class);

		UserGroup[] userGroups = mapper.readValue(responseBody,
				UserGroup[].class);
        */
    }
}
