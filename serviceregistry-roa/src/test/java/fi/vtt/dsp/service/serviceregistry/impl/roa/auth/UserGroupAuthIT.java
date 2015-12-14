package fi.vtt.dsp.service.serviceregistry.impl.roa.auth;

import fi.vtt.dsp.service.serviceregistry.auth.token.AccessToken;
import fi.vtt.dsp.service.serviceregistry.common.GroupRole;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.UserGroup;
import fi.vtt.dsp.service.serviceregistry.common.UserProfile;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.impl.roa.BaseAuthIT;
import fi.vtt.dsp.service.serviceregistry.impl.roa.TestAccessTokenFactory;
import java.io.IOException;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserGroupAuthIT extends BaseAuthIT {
	private static final Logger LOGGER = Logger.getLogger(UserGroupAuthIT.class
			.getName());

	protected UserProfile insiderUserProfileWriter;
	protected UserProfile insiderUserProfileReader;
	protected UserProfile outsiderUserProfile;

	protected ServiceRegistryEntry groupService;
	protected ServiceRegistryEntry privateServiceOutsideOfGroup;
	protected ServiceRegistryEntry publicServiceOutsideOfGroup;

	protected ServiceInstance groupInstance;
	protected ServiceInstance privateInstanceOutsideOfGroup;
	protected ServiceInstance publicInstanceOutsideOfGroup;

	protected UserGroup userGroup;
	protected GroupRole groupRoleAgent;
	protected GroupRole groupRoleInsiderWriter;
	protected GroupRole groupRoleInsiderReader;

	@Before
	public void setUpTest() throws JsonGenerationException,
			JsonMappingException, IOException {
		insiderUserProfileWriter = createUserProfile();
		insiderUserProfileReader = createUserProfile();
		outsiderUserProfile = createUserProfile();

		groupService = createService(userAgent.getUserId());
		groupService.getServiceDescription().setOwnerGroup("group");
		groupService = updateService(groupService);

		privateServiceOutsideOfGroup = createService(userAgent.getUserId());
		privateServiceOutsideOfGroup.getServiceDescription().setOwnerGroup(
				"private");
		privateServiceOutsideOfGroup = updateService(privateServiceOutsideOfGroup);

		publicServiceOutsideOfGroup = createService(userAgent.getUserId());
		publicServiceOutsideOfGroup.getServiceDescription().setOwnerGroup(
				"public");
		publicServiceOutsideOfGroup = updateService(publicServiceOutsideOfGroup);

		groupInstance = createServiceInstance(userAgent.getUserId(),
				groupService.getServiceId());
		privateInstanceOutsideOfGroup = createServiceInstance(
				userAgent.getUserId(),
				privateServiceOutsideOfGroup.getServiceId());
		publicInstanceOutsideOfGroup = createServiceInstance(
				userAgent.getUserId(),
				publicServiceOutsideOfGroup.getServiceId());

		userGroup = new UserGroup();

		userGroup.getServiceRegistryEntryId().add(groupService.getServiceId());

		groupRoleInsiderWriter = new GroupRole();
		groupRoleInsiderReader = new GroupRole();

		groupRoleInsiderWriter.setAccessRights("write");
		groupRoleInsiderReader.setAccessRights("reader");

		groupRoleInsiderWriter.setUserId(insiderUserProfileWriter.getUserId());
		groupRoleInsiderReader.setUserId(insiderUserProfileReader.getUserId());

		userGroup.getGroupRole().add(groupRoleInsiderWriter);
		userGroup.getGroupRole().add(groupRoleInsiderReader);

		createUserGroup(userGroup);
	}

	/*
	 * @Test public void test_UpdateOwn_RegisteredService() throws
	 * JsonGenerationException, JsonMappingException, IOException {
	 * 
	 * Response r = sendUpdate(getTestResourcePath(), getTestResource(),
	 * getRegisteredAgentAT(serviceAgent));
	 * 
	 * int expectedStatus = getExpectedTestReqStatus(TestReq.UPD_OWN,
	 * AgentType.SERVICE, AgentRole.ROLE_REGISTERED);
	 * assertEquals(expectedStatus, r.getStatus()); }
	 */
	public String getServicePath(ServiceRegistryEntry serviceRegistryEntry) {
		return String.format(PATH_SERVICE, serviceRegistryEntry.getServiceId());
	}

	public String getServiceInstancePath(
			ServiceRegistryEntry serviceRegistryEntry,
			ServiceInstance serviceInstance) {
		return String.format(PATH_SERVICE_INSTANCE,
				serviceRegistryEntry.getServiceId(),
				serviceInstance.getServiceInstanceId());
	}

	@Test
	public void test_updatePrivateService_outsiderUser()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendUpdate(
				getServicePath(privateServiceOutsideOfGroup),
				privateServiceOutsideOfGroup,
				TestAccessTokenFactory.getRegisteredAgentAT(outsiderUserProfile));
		assertEquals(403, response.getStatus());
	}

	@Test
	public void test_updateGroupService_outsiderUser()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendUpdate(getServicePath(groupService),
				groupService, TestAccessTokenFactory.getRegisteredAgentAT(outsiderUserProfile));
		assertEquals(403, response.getStatus());
	}

	@Test
	public void test_updateGroupService_insiderUserWriter()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendUpdate(getServicePath(groupService),
				groupService, TestAccessTokenFactory.getRegisteredAgentAT(insiderUserProfileWriter));
		assertEquals(200, response.getStatus());
	}

	@Test
	public void test_updateGroupService_insiderUserReader()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendUpdate(getServicePath(groupService),
				groupService, TestAccessTokenFactory.getRegisteredAgentAT(insiderUserProfileReader));
		assertEquals(403, response.getStatus());
	}

	@Test
	public void test_updatePublicService_insiderUserReader()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendUpdate(
				getServicePath(publicServiceOutsideOfGroup),
				publicServiceOutsideOfGroup,
				TestAccessTokenFactory.getRegisteredAgentAT(insiderUserProfileReader));
		assertEquals(403, response.getStatus());
	}

	// Instance tests
	@Test
	public void test_updatePrivateServiceInstance_outsiderUser()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendUpdate(
				getServiceInstancePath(privateServiceOutsideOfGroup,
						privateInstanceOutsideOfGroup),
				privateInstanceOutsideOfGroup,
				TestAccessTokenFactory.getRegisteredAgentAT(outsiderUserProfile));
		assertEquals(403, response.getStatus());
	}

	@Test
	public void test_updateGroupServiceInstance_outsiderUser()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendUpdate(
				getServiceInstancePath(groupService, groupInstance),
				groupInstance, TestAccessTokenFactory.getRegisteredAgentAT(outsiderUserProfile));
		assertEquals(403, response.getStatus());
	}

	@Test
	public void test_updateGroupServiceInstance_insiderUserWriter()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendUpdate(
				getServiceInstancePath(groupService, groupInstance),
				groupInstance, TestAccessTokenFactory.getRegisteredAgentAT(insiderUserProfileWriter));
		assertEquals(200, response.getStatus());
	}

	@Test
	public void test_updateGroupServiceInstance_insiderUserReader()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendUpdate(
				getServiceInstancePath(groupService, groupInstance),
				groupInstance, TestAccessTokenFactory.getRegisteredAgentAT(insiderUserProfileReader));
		assertEquals(403, response.getStatus());
	}

	@Test
	public void test_updatePublicServiceInstance_insiderUserReader()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendUpdate(
				getServiceInstancePath(publicServiceOutsideOfGroup,
						publicInstanceOutsideOfGroup),
				publicInstanceOutsideOfGroup,
				TestAccessTokenFactory.getRegisteredAgentAT(insiderUserProfileReader));
		assertEquals(403, response.getStatus());
	}

	// Delete tests
	@Test
	public void test_deletePrivateServiceInstance_outsiderUser()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendDelete(
				getServiceInstancePath(privateServiceOutsideOfGroup,
						privateInstanceOutsideOfGroup),
				TestAccessTokenFactory.getRegisteredAgentAT(outsiderUserProfile));
		assertEquals(403, response.getStatus());
	}

	@Test
	public void test_deleteGroupServiceInstance_outsiderUser()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendDelete(
				getServiceInstancePath(groupService, groupInstance),
				TestAccessTokenFactory.getRegisteredAgentAT(outsiderUserProfile));
		assertEquals(403, response.getStatus());
	}

	@Test
	public void test_deleteGroupServiceInstance_insiderUserReader()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendDelete(
				getServiceInstancePath(groupService, groupInstance),
				TestAccessTokenFactory.getRegisteredAgentAT(insiderUserProfileReader));
		assertEquals(403, response.getStatus());
	}

	@Test
	public void test_deletePublicServiceInstance_insiderUserReader()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendDelete(
				getServiceInstancePath(publicServiceOutsideOfGroup,
						publicInstanceOutsideOfGroup),
				TestAccessTokenFactory.getRegisteredAgentAT(insiderUserProfileReader));
		assertEquals(403, response.getStatus());
	}

	@Test
	public void test_deleteGroupServiceInstance_insiderUserWriter()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendDelete(
				getServiceInstancePath(groupService, groupInstance),
				TestAccessTokenFactory.getRegisteredAgentAT(insiderUserProfileWriter));
		assertEquals(200, response.getStatus());
	}

	// Delete test service
	@Test
	public void test_deletePrivateService_outsiderUser()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendDelete(
				getServicePath(privateServiceOutsideOfGroup),
				TestAccessTokenFactory.getRegisteredAgentAT(outsiderUserProfile));
		assertEquals(403, response.getStatus());
	}

	@Test
	public void test_deleteGroupService_outsiderUser()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendDelete(getServicePath(groupService),
				TestAccessTokenFactory.getRegisteredAgentAT(outsiderUserProfile));
		assertEquals(403, response.getStatus());
	}

	@Test
	public void test_deleteGroupService_insiderUserReader()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendDelete(getServicePath(groupService),
				TestAccessTokenFactory.getRegisteredAgentAT(insiderUserProfileReader));
		assertEquals(403, response.getStatus());
	}

	@Test
	public void test_deletePublicService_insiderUserReader()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendDelete(
				getServicePath(publicServiceOutsideOfGroup),
				TestAccessTokenFactory.getRegisteredAgentAT(insiderUserProfileReader));
		assertEquals(403, response.getStatus());
	}

	@Test
	public void test_deleteGroupService_insiderUserWriter()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response response = sendDelete(getServicePath(groupService),
				TestAccessTokenFactory.getRegisteredAgentAT(insiderUserProfileWriter));
		assertEquals(200, response.getStatus());
	}

	@After
	public void cleanUpTest() {
		if (insiderUserProfileWriter.getUserId() != null) {
			deleteProfile(insiderUserProfileWriter.getUserId());
		}

		if (insiderUserProfileReader.getUserId() != null) {
			deleteProfile(insiderUserProfileReader.getUserId());
		}

		if (outsiderUserProfile.getUserId() != null) {
			deleteProfile(outsiderUserProfile.getUserId());
		}

		if (groupService.getServiceId() != null) {
			deleteService(groupService.getServiceId());
		}

		if (privateServiceOutsideOfGroup.getServiceId() != null) {
			deleteService(privateServiceOutsideOfGroup.getServiceId());
		}

		if (publicServiceOutsideOfGroup.getServiceId() != null) {
			deleteService(publicServiceOutsideOfGroup.getServiceId());
		}

		if (userGroup.getUserGroupId() != null) {
			deleteUserGroup(userGroup.getUserGroupId());
		}
	}

	private Response sendUpdate(String resourcePath, Object resource,
			AccessToken token) throws JsonGenerationException,
			JsonMappingException, IOException {
		WebClient client;

		if (token != null) {
			client = setupJSONClient(resourcePath, token);
		} else {
			client = setupJSONClient(resourcePath);
		}

		Response response = client.put(MAPPER.writeValueAsString(resource));

		return response;
	}

	private Response sendDelete(String resourcePath, AccessToken token)
			throws JsonGenerationException, JsonMappingException, IOException {
		WebClient client;

		if (token != null) {
			client = setupJSONClient(resourcePath, token);
		} else {
			client = setupJSONClient(resourcePath);
		}

		Response response = client.delete();

		return response;
	}
}
