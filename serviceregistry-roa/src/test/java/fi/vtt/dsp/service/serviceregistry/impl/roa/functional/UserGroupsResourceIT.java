package fi.vtt.dsp.service.serviceregistry.impl.roa.functional;

import fi.vtt.dsp.service.serviceregistry.TestData;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.UserProfile;
import fi.vtt.dsp.service.serviceregistry.common.UserGroup;
import fi.vtt.dsp.service.serviceregistry.common.GroupRole;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotSavedFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOUpdateFailedFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.TestDataSetter;
import fi.vtt.dsp.service.serviceregistry.impl.roa.BaseResourceIT;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UserGroupsResourceIT extends BaseResourceIT {
	private static final Logger LOGGER = Logger
			.getLogger(UserGroupsResourceIT.class.getName());
	private UserProfile userProfile = new UserProfile();
	private UserGroup userGroup = new UserGroup();
	private GroupRole groupRole = new GroupRole();
	private String updatedUserGroupName = "update user-group name jeeeaaps";

	private void setUpTestDataObjects() {
		userProfile.setFirstName("Testi");
		userProfile.setLastName("Hemmo");
		userProfile.setEmail("validi@sahkopostiosote.com");

		userGroup.setName("Omituiste otuste kerho");
	}

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
		WebClient client = this.setUpJSONClient("/resourcedirectory/v1/users");
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValueAsString(userProfile);

		Response r = client.post(userProfile);
		LOGGER.log(Level.FINE, "Posting user-profile");
		Assert.assertEquals(201, r.getStatus());

		String userURI = r.getLocation().toString();
		LOGGER.log(Level.FINE, "Making sure URI is not null " + userURI);
		Assert.assertNotNull(userURI);
		String userId = userURI.substring(userURI.lastIndexOf("/") + 1);
		client.close();
		userProfile.setUserId(userId);

		LOGGER.log(Level.FINE, "User ID: " + userProfile.getUserId());

		groupRole.setUserId(userProfile.getUserId());
		groupRole.setAccessRights("write");

		userGroup.getGroupRole().add(groupRole);

		client = setUpJSONClient("/resourcedirectory/v1/usergroups");
		mapper = new ObjectMapper();
		String postJSONString = mapper.writeValueAsString(userGroup);

		Response response = client.post(postJSONString);
		LOGGER.log(Level.FINE, "Posting user-group");
		Assert.assertEquals(201, response.getStatus());

		userGroup
				.setUserGroupId(response
						.getLocation()
						.toString()
						.substring(
								response.getLocation().toString()
										.lastIndexOf("/") + 1));

		// Update name
		userGroup.setName(updatedUserGroupName);
		client = setUpJSONClient("/resourcedirectory/v1/usergroups/"
				+ userGroup.getUserGroupId());
		mapper = new ObjectMapper();
		postJSONString = mapper.writeValueAsString(userGroup);

		response = client.put(postJSONString);
		LOGGER.log(Level.FINE, "Updating name");
		Assert.assertEquals(200, response.getStatus());

		// Get it
		client = setUpJSONClient("/resourcedirectory/v1/usergroups");
		mapper = new ObjectMapper();

		response = client.get();
		String responseBody = response.readEntity(String.class);

		UserGroup[] userGroups = mapper.readValue(responseBody,
				UserGroup[].class);

		for (UserGroup userGroupIter : userGroups) {
			if (userGroupIter.getUserGroupId().equals(
					userGroup.getUserGroupId())) {
				userGroup = userGroupIter;
				break;
			}
		}

		LOGGER.log(Level.FINE, "Checking group-name");
		Assert.assertEquals(userGroup.getName(), updatedUserGroupName);
		
		String userGroupId = userGroup.getUserGroupId();

		client = setUpJSONClient("/resourcedirectory/v1/usergroups/user/"
				+ userProfile.getUserId());
		mapper = new ObjectMapper();

		response = client.get();
		responseBody = response.readEntity(String.class);
		UserGroup[] userGroups2 = mapper.readValue(responseBody,
				UserGroup[].class);

		LOGGER.log(Level.FINE, "Checking group-size");
		Assert.assertNotEquals(0, userGroups2.length);

		LOGGER.log(Level.FINE, "Group ID: " + userGroupId);
		
		// Test getAllServicesForGroup
		client = setUpJSONClient("/resourcedirectory/v1/usergroups/"+userGroupId+"/servicesingroup");
		response = client.get();
		responseBody = response.readEntity(String.class);
		LOGGER.log(Level.FINE, "response: "+responseBody);
		mapper = new ObjectMapper();
		ServiceRegistryEntry[] serviceRegistryEntrys = mapper.readValue(responseBody, ServiceRegistryEntry[].class);
		
		Assert.assertEquals(0, serviceRegistryEntrys.length);
		
		client = setUpJSONClient("/resourcedirectory/v1/serviceregistrations");
		ServiceRegistryEntry serviceRegistryEntry = TestData.createServiceRegistryEntry();
		serviceRegistryEntry.getServiceDescription().setCreatedByUserId(userProfile.getUserId());
		serviceRegistryEntry.getServiceInstance().clear();
		
		LOGGER.log(Level.FINE, "User id: " + userProfile.getUserId());

		mapper = new ObjectMapper();
		postJSONString = mapper.writeValueAsString(serviceRegistryEntry);

		response = client.post(postJSONString);
		LOGGER.log(Level.FINE, "Posting service");

		Assert.assertEquals(201, response.getStatus());

		String serviceURI = response.getLocation().toString();
		LOGGER.log(Level.FINE, "Making sure URI is not null " + serviceURI);
		Assert.assertNotNull(serviceURI);
		String serviceId = serviceURI.substring(serviceURI.lastIndexOf("/") + 1);

		
		userGroup.getServiceRegistryEntryId().add(serviceId);
		client = setUpJSONClient("/resourcedirectory/v1/usergroups/"
				+ userGroup.getUserGroupId());
		mapper = new ObjectMapper();
		postJSONString = mapper.writeValueAsString(userGroup);

		response = client.put(postJSONString);
		LOGGER.log(Level.FINE, "Updating name");
		Assert.assertEquals(200, response.getStatus());
		
		client = setUpJSONClient("/resourcedirectory/v1/usergroups/"+userGroupId+"/servicesingroup");
		response = client.get();
		responseBody = response.readEntity(String.class);
		serviceRegistryEntrys = mapper.readValue(responseBody, ServiceRegistryEntry[].class);
		
		Assert.assertEquals(1, serviceRegistryEntrys.length);
		
		// Clean-up
		client = setUpJSONClient("/resourcedirectory/v1/usergroups/"
				+ userGroup.getUserGroupId());

		response = client.delete();

		LOGGER.log(Level.FINE,
				"Response code for delete: " + response.getStatus());

		LOGGER.log(Level.FINE, "Posting user-profile");
		Assert.assertEquals(200, response.getStatus());

		client = this.setUpJSONClient("/resourcedirectory/v1/users/"
				+ userProfile.getUserId());
		r = client.delete();
		LOGGER.log(Level.FINE, "Trying to delete user-profile");
		Assert.assertEquals(200, r.getStatus());
		client.close();
	}
}
