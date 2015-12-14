package fi.vtt.dsp.service.serviceregistry.impl.roa.functional;

import fi.vtt.dsp.service.serviceregistry.impl.roa.BaseResourceIT;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import fi.vtt.dsp.service.serviceregistry.common.description.UserFeedback;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBConnection;
import fi.vtt.dsp.service.serviceregistry.TestProperties;
import fi.vtt.dsp.service.serviceregistry.TestProperty;

public class UserFeedbacksResourceIT extends BaseResourceIT {

	private static final Logger LOGGER = Logger
			.getLogger(UserFeedbacksResourceIT.class.getName());

	@Test
	public void createNewUserFeedback() throws IOException {
		try {
			UserFeedback uf = new UserFeedback();
			uf.setProvidedByUserId("1");
			uf.setFeedback("Feedback");
			uf.setUserRating(8);

			WebClient client = this
					.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
							+ id + "/servicedescription/userfeedbacks");
			ObjectMapper mapper = new ObjectMapper();
			String postJSONString = mapper.writeValueAsString(uf);

			Response r = client.post(uf);
			Assert.assertEquals(201, r.getStatus());

			String userFeedbackURI = r.getLocation().toString();
			Assert.assertNotNull(userFeedbackURI);
			String userFeedbackId = userFeedbackURI.substring(userFeedbackURI
					.lastIndexOf("/") + 1);
			client.close();

			client = this
					.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
							+ id
							+ "/servicedescription/userfeedbacks/"
							+ userFeedbackId);
			r = client.get();

			Assert.assertEquals(200, r.getStatus());
			String responseBody = r.readEntity(String.class);
			mapper = new ObjectMapper();

			UserFeedback uf2 = mapper.readValue(responseBody,
					UserFeedback.class);

			Assert.assertEquals(uf.getProvidedByUserId(),
					uf2.getProvidedByUserId());
			Assert.assertEquals(uf.getFeedback(), uf2.getFeedback());
			Assert.assertEquals(uf.getUserRating(), uf2.getUserRating());
			client.close();

			// remove from database
			MongoDBConnection dbConn = new MongoDBConnection();
			dbConn.setDbServerIP(TestProperties.get(TestProperty.DB_ADDRESS));
			dbConn.setDbServerPort(Integer.parseInt(TestProperties.get(TestProperty.DB_PORT)));
			dbConn.setDbUserName(TestProperties.get(TestProperty.DB_USER_NAME));
			dbConn.setDbPassword(TestProperties.get(TestProperty.DB_USER_PWD));
			dbConn.setDbName(TestProperties.get(TestProperty.DB_NAME));
			dbConn.setDbCollectionName("serviceregistry");

			DBCollection dbColl = dbConn.getDBCollection();
			BasicDBObject serviceRegistryEntryPointer = new BasicDBObject(
					"_id", new ObjectId(id));
			BasicDBObject itemToBeDeleted = new BasicDBObject("userFeedbackId",
					userFeedbackId);
			BasicDBObject arrayQuery = new BasicDBObject("$elemMatch",
					itemToBeDeleted);
			serviceRegistryEntryPointer.append(
					"serviceDescription.userFeedback", arrayQuery);
			DBObject dbObj = dbColl.findOne(serviceRegistryEntryPointer);
			Assert.assertNotNull(dbObj);

			/*
			 * WriteResult wr = dbColl.remove(dbObj, WriteConcern.SAFE);
			 * if(wr.getError() != null || wr.getN() == 0) { throw new
			 * DAOGeneralSystemFault(wr.getError(), null); }
			 */
		} catch (DAOGeneralSystemFault e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
	}
}
