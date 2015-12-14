package fi.vtt.dsp.service.serviceregistry.impl.roa.functional;

import fi.vtt.dsp.service.serviceregistry.impl.roa.BaseResourceIT;
import java.io.IOException;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.UserProfile;
import fi.vtt.dsp.service.serviceregistry.TestData;
import fi.vtt.dsp.service.serviceregistry.impl.dao.TestDataSetter;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotSavedFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOUpdateFailedFault;

public class UserProfilesResourceIT extends BaseResourceIT {

	@Before
	@Override
	public void setDB() throws DAOGeneralSystemFault, DAONotSavedFault,
			DAONotFoundFault, DAOUpdateFailedFault {
		TestDataSetter tDSetter = new TestDataSetter();
		tDSetter.cleanUserProfiles();
		tDSetter.cleanRegistryEntries();
		ServiceRegistryEntry sRegEntr = tDSetter.setValidRegistryEntries();
		id = tDSetter.setValidUserProfile1();
	}

	@Test
	public void createNewUserProfile() throws IOException {
		UserProfile up = getUserProfileForTesting();
		deleteExisting(up);

		WebClient client = this.setUpJSONClient("resourcedirectory/v1/users");
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValueAsString(up);

		Response r = client.post(up);
		Assert.assertEquals(201, r.getStatus());

		String userURI = r.getLocation().toString();
		Assert.assertNotNull(userURI);
		String userId = userURI.substring(userURI.lastIndexOf("/") + 1);
		client.close();
		up.setUserId(userId);
		client = this.setUpJSONClient("resourcedirectory/v1/users/"
				+ up.getUserId());
		r = client.get();

		Assert.assertEquals(200, r.getStatus());
		String responseBody = r.readEntity(String.class);
		mapper = new ObjectMapper();

		UserProfile up2 = mapper.readValue(responseBody, UserProfile.class);

		Assert.assertEquals(up.getFirstName(), up2.getFirstName());
		client.close();
	}

	@Test
	@Ignore("Ignored until validation re-designed")
	public void createInvalidUserProfile() throws IOException {
		UserProfile up = getUserProfileForTesting();
		deleteExisting(up);

		up.setEmail("peuukku3@@jj.fi"); // invalid email
		WebClient client = this.setUpJSONClient("resourcedirectory/v1/users");
		ObjectMapper mapper = new ObjectMapper();
		String postJSONString = mapper.writeValueAsString(up);
		Response r = client.post(postJSONString);
		Assert.assertEquals(400, r.getStatus());
		client.close();
		up.setEmail("peuukku3@jj.fi");
	}

	@Test
	public void updateUserProfile() throws IOException {
		UserProfile up = TestData.getUserProfile1();
		up.setUserId(id);
		up.setPreferredLanguage("english");

		WebClient client = this.setUpJSONClient("resourcedirectory/v1/users/"
				+ id);
		ObjectMapper mapper = new ObjectMapper();
		String postJSONString = mapper.writeValueAsString(up);
		Response r = client.put(postJSONString);

		Assert.assertEquals(200, r.getStatus());

		client.close();
		up.setPreferredLanguage("finnish");
	}

	@Test
	@Ignore("Ignored until validation re-designed")
	public void updateInvalidUserProfile() throws IOException {
		UserProfile up = getUserProfileForTesting();
		deleteExisting(up);

		WebClient client = this.setUpJSONClient("resourcedirectory/v1/users");
		ObjectMapper mapper = new ObjectMapper();
		String postJSONString = mapper.writeValueAsString(up);
		Response r = client.post(postJSONString);
		Assert.assertEquals(201, r.getStatus());
		String userURI = r.getLocation().toString();
		Assert.assertNotNull(userURI);
		String userId = userURI.substring(userURI.lastIndexOf("/") + 1);
		client.close();

		up.setUserId(userId);
		up.setPreferredLanguage("A VERY LONG LANGUAGE STRING THAT DOES NOT ACTUALLY EXIST");
		client = this.setUpJSONClient("resourcedirectory/v1/users/" + userId);
		mapper = new ObjectMapper();
		postJSONString = mapper.writeValueAsString(up);
		r = client.put(postJSONString);
		Assert.assertEquals(400, r.getStatus());
		client.close();
		up.setPreferredLanguage("finnish");
	}

	@Test
	public void deleteUserProfile() throws IOException {
		UserProfile up = TestData.getUserProfile1();
		up.setUserId(id);
		WebClient client = this.setUpJSONClient("resourcedirectory/v1/users/"
				+ id);
		Response r = client.delete();
		Assert.assertEquals(200, r.getStatus());
		client.close();
	}

	@Test
	@Ignore("Obsolete?")
	public void getAllUserProfiles() throws IOException {
		UserProfile up = getUserProfileForTesting();
		deleteExisting(up);

		WebClient client = this.setUpJSONClient("resourcedirectory/v1/users");
		ObjectMapper mapper = new ObjectMapper();
		String postJSONString = mapper.writeValueAsString(up);
		Response r = client.post(postJSONString);
		Assert.assertEquals(201, r.getStatus());
		String userURI = r.getLocation().toString();
		Assert.assertNotNull(userURI);
		String userId = userURI.substring(userURI.lastIndexOf("/") + 1);
		client.close();

		client = this.setUpJSONClient("resourcedirectory/v1/users");
		r = client.get();
		Assert.assertEquals(200, r.getStatus());
		String responseBody = r.readEntity(String.class);
		mapper = new ObjectMapper();
		UserProfile[] userProfilesArr = mapper.readValue(responseBody,
				UserProfile[].class);
		client.close();
		Assert.assertTrue(userProfilesArr.length > 0);

		client = this.setUpJSONClient("resourcedirectory/v1/user/" + userId);
		r = client.delete();
		Assert.assertEquals(200, r.getStatus());
		client.close();
	}

	private UserProfile getUserProfileForTesting() {
		UserProfile up = new UserProfile();
		up.setEmail("peuukku3@jj.fi");
		up.setFirstName("pekka");
		up.setMiddleNames("aarne");
		up.setLastName("pääkkönen");
		up.setCountryCode("FI");
		up.setPreferredLanguage("finnish");
		up.setOrganization("VTT");
		up.setOrganizationalUnit("KIPS");
		up.setTelephone("+358443094");
		return up;
	}

	private void deleteExisting(UserProfile up) {
		// delete if exists
		/*
		 * if(up.getUserId()!= null) { WebClient client =
		 * this.setUpJSONClient("resourcedirectory/v1/users/"+up.getUserId());
		 * Response r = client.delete(); client.close(); }
		 */
	}

}
