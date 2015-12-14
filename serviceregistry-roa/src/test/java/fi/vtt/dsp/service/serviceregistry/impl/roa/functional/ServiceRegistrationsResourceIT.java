package fi.vtt.dsp.service.serviceregistry.impl.roa.functional;

import fi.vtt.dsp.service.serviceregistry.impl.roa.BaseResourceIT;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.*;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import fi.vtt.dsp.service.serviceregistry.TestDb;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.TestData;
import fi.vtt.dsp.service.serviceregistry.impl.dao.TestDataSetter;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotSavedFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOUpdateFailedFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOUtils;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBConnection;
import fi.vtt.dsp.serviceframework.common.Binding;

public class ServiceRegistrationsResourceIT extends BaseResourceIT {

	@Test
	public void getAllServiceRegistrations() throws JsonParseException,
			JsonMappingException, IOException {
		// "resourcedirectory/v1/serviceregistrations/"
		WebClient client = this
				.setUpJSONClient("resourcedirectory/v1/serviceregistrations");
		Response r = client.get();
		Assert.assertEquals(200, r.getStatus());
		String responseBody = r.readEntity(String.class);
		ObjectMapper mapper = new ObjectMapper();
		List<ServiceRegistryEntry> sRegs = mapper.readValue(responseBody,
				List.class);
		Assert.assertTrue(sRegs.size() > 0);
		client.close();
	}

	@Test
	public void getOneServiceRegistration() throws IOException {
		WebClient client = this
				.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
						+ id);
		Response r = client.get();
		Assert.assertEquals(200, r.getStatus());
		String responseBody = r.readEntity(String.class);
		ObjectMapper mapper = new ObjectMapper();
		ServiceRegistryEntry sReg = mapper.readValue(responseBody,
				ServiceRegistryEntry.class);

		Assert.assertEquals(sReg.getServiceId(), id);

		client.close();
	}

	@Test
	public void postNewServiceRegistration() throws DAOGeneralSystemFault,
			JsonGenerationException, JsonMappingException, IOException,
			DAOUpdateFailedFault, DAONotSavedFault, DAONotFoundFault {
		WebClient client = this
				.setUpJSONClient("resourcedirectory/v1/serviceregistrations/");
		ObjectMapper mapper = new ObjectMapper();

		ServiceRegistryEntry testSRE = TestData.getSample1RegEntry();
		TestDataSetter tDSetter = new TestDataSetter();
		String validUserId = tDSetter.setValidUserProfile1();
		testSRE.getServiceDescription().setCreatedByUserId(validUserId);

		for (ServiceInstance sI : testSRE.getServiceInstance()) {
			sI.setCreatedByUserId(validUserId);

			for (Binding bI : sI.getServiceAccessEndPoint().getBinding()) {
				bI.setAuthorizedByUserId(validUserId);
			}
		}

		String postJSONString = mapper.writeValueAsString(testSRE);
		Response r = client.post(postJSONString);

		Assert.assertEquals(201, r.getStatus());

		String locationOfNewServiceReg = r.getHeaderString("Location");
		Assert.assertNotNull(locationOfNewServiceReg);

		// Check database for insertion
		String[] serviceURLTokens = locationOfNewServiceReg.split("\\/");
		String serviceId = serviceURLTokens[serviceURLTokens.length - 1];

		MongoDBConnection dbConn = TestDb.getDbConnection("serviceregistry");
		DBCollection dbColl = dbConn.getDBCollection();
		DBObject dbObj = dbColl.findOne(new ObjectId(serviceId));
		Assert.assertNotNull(dbObj);

		// Remove extra test data from DB
		ServiceRegistryEntry dbEntry = DAOUtils.convertDBObjToWSObjViaJSON(
				dbObj, ServiceRegistryEntry.class);
		Assert.assertEquals(serviceId, dbEntry.getServiceId());

		WriteResult wr = dbColl.remove(dbObj, WriteConcern.SAFE);
		if (wr.getError() != null || wr.getN() == 0) {
			throw new DAOGeneralSystemFault(wr.getError(), null);
		}
	}

	@Test
	public void postNewServiceRegistrationFailsDueMissingCreatedByUserId()
			throws JsonGenerationException, JsonMappingException, IOException,
			DAOGeneralSystemFault, DAONotFoundFault {
		WebClient client = this
				.setUpJSONClient("resourcedirectory/v1/serviceregistrations/");
		ObjectMapper mapper = new ObjectMapper();
		String postJSONString = mapper.writeValueAsString(TestData
				.getSample1RegEntry());

		Response r = client.post(postJSONString);
		Assert.assertEquals(400, r.getStatus());
	}
}
