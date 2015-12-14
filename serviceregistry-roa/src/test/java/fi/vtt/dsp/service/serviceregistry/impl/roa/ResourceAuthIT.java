package fi.vtt.dsp.service.serviceregistry.impl.roa;

import fi.vtt.dsp.service.serviceregistry.auth.token.AccessToken;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import static org.junit.Assert.*;

import org.junit.Test;

import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import java.util.logging.Logger;

public abstract class ResourceAuthIT<T> extends BaseAuthIT {
	private static final Logger LOGGER = Logger.getLogger(ResourceAuthIT.class.getName());
	public static enum TestReq {
		UPD_OWN, UPD_OTHER, DEL_OWN, DEL_OTHER,
	};

	private final Map<String, Integer> expectedResponses = new HashMap<>();

	public ResourceAuthIT() {

		// default response statuses
		setExpectedTestReqStatus(TestReq.UPD_OWN, AgentType.USER,
				AgentRole.ROLE_ADMIN, Response.Status.OK);
		setExpectedTestReqStatus(TestReq.UPD_OWN, AgentType.SERVICE,
				AgentRole.ROLE_ADMIN, Response.Status.OK);
		setExpectedTestReqStatus(TestReq.UPD_OWN, AgentType.SERVICE_INSTANCE,
				AgentRole.ROLE_ADMIN, Response.Status.OK);
		setExpectedTestReqStatus(TestReq.UPD_OTHER, AgentType.USER,
				AgentRole.ROLE_ADMIN, Response.Status.OK);
		setExpectedTestReqStatus(TestReq.UPD_OTHER, AgentType.SERVICE,
				AgentRole.ROLE_ADMIN, Response.Status.OK);
		setExpectedTestReqStatus(TestReq.UPD_OTHER, AgentType.SERVICE_INSTANCE,
				AgentRole.ROLE_ADMIN, Response.Status.OK);

		setExpectedTestReqStatus(TestReq.DEL_OWN, AgentType.USER,
				AgentRole.ROLE_ADMIN, Response.Status.OK);
		setExpectedTestReqStatus(TestReq.DEL_OWN, AgentType.SERVICE,
				AgentRole.ROLE_ADMIN, Response.Status.OK);
		setExpectedTestReqStatus(TestReq.DEL_OWN, AgentType.SERVICE_INSTANCE,
				AgentRole.ROLE_ADMIN, Response.Status.OK);
		setExpectedTestReqStatus(TestReq.DEL_OTHER, AgentType.USER,
				AgentRole.ROLE_ADMIN, Response.Status.OK);
		setExpectedTestReqStatus(TestReq.DEL_OTHER, AgentType.SERVICE,
				AgentRole.ROLE_ADMIN, Response.Status.OK);
		setExpectedTestReqStatus(TestReq.DEL_OTHER, AgentType.SERVICE_INSTANCE,
				AgentRole.ROLE_ADMIN, Response.Status.OK);

		setExpectedTestReqStatus(TestReq.UPD_OWN, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.UPD_OWN, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.UPD_OWN, AgentType.SERVICE_INSTANCE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.UPD_OTHER, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.UPD_OTHER, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.UPD_OTHER, AgentType.SERVICE_INSTANCE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);

		setExpectedTestReqStatus(TestReq.DEL_OWN, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.DEL_OWN, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.DEL_OWN, AgentType.SERVICE_INSTANCE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.DEL_OTHER, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.DEL_OTHER, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.DEL_OTHER, AgentType.SERVICE_INSTANCE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);

	}

	public final void setExpectedTestReqStatus(TestReq req, AgentType type,
			AgentRole role, Response.Status status) {

		String key = req + "" + type + "" + role;
		expectedResponses.put(key, status.getStatusCode());
	}

	public int getExpectedTestReqStatus(TestReq req, AgentType type,
			AgentRole role) {

		String key = req + "" + type + "" + role;
		return expectedResponses.get(key);
	}

	protected abstract T getTestResource();

	protected abstract T getOtherTestResource();

	protected abstract String getTestResourcePath();

	protected abstract String getOtherTestResourcePath();

	protected Response sendUpdate(String resourcePath, T resource, AccessToken token) throws JsonGenerationException,
			JsonMappingException, IOException {

		WebClient client = null;

		if (token != null) {
			client = setupJSONClient(resourcePath, token);
		} else {
			client = setupJSONClient(resourcePath);
		}

		Response r = client.put(MAPPER.writeValueAsString(resource));

		return r;
	}

	private Response sendDelete(String resourcePath, AccessToken token)
			throws JsonGenerationException, JsonMappingException, IOException {

		WebClient client = null;

		if (token != null) {
			client = setupJSONClient(resourcePath, token);
		} else {
			client = setupJSONClient(resourcePath);
		}

		Response r = client.delete();

		return r;
	}

	//
	// UNAUTHORIZED UPDATE TESTS
	//
	@Test
	public void test_Update_WithoutAccessToken()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendUpdate(getTestResourcePath(), getTestResource(), null);

		assertEquals(401, r.getStatus());

	}

	@Test
	public void test_Update_WithInvalidAccessToken()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendUpdate(getTestResourcePath(), getTestResource(), INVALID_AT);

		assertEquals(401, r.getStatus());

	}

	@Test
	public void test_Update_WithUnauthenticAccessToken()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendUpdate(getTestResourcePath(), getTestResource(), INAUTHENTIC_AT);

		assertEquals(401, r.getStatus());

	}

	//
	// UPDATE TESTS RESOURCE NOT FOUND
	//
	@Test
	public void test_UpdateNotFound() throws JsonGenerationException,
			JsonMappingException, IOException {
		String invalidPath = getTestResourcePath();
		invalidPath = invalidPath
				.substring(0, invalidPath.lastIndexOf("/") + 1)
				+ "123456789012345678901234";
		Response r = sendUpdate(invalidPath, getTestResource(), USER_ADMIN_AT);

		assertEquals(404, r.getStatus());
	}

	//
	// UPDATE TESTS WITH USER AGENT
	//
	@Test
	public void test_UpdateOwn_RegisteredUser() throws JsonGenerationException,
			JsonMappingException, IOException {

		Response r = sendUpdate(getTestResourcePath(), getTestResource(),
				TestAccessTokenFactory.getRegisteredAgentAT(userAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.UPD_OWN,
				AgentType.USER, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_UpdateOther_RegisteredUser()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response r = sendUpdate(getOtherTestResourcePath(),
				getOtherTestResource(), TestAccessTokenFactory.getRegisteredAgentAT(userAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.UPD_OTHER,
				AgentType.USER, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_UpdateOwn_AdminUser() throws JsonGenerationException,
			JsonMappingException, IOException {

		Response r = sendUpdate(getTestResourcePath(), getTestResource(),
				TestAccessTokenFactory.getAdminAgentAT(userAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.UPD_OWN,
				AgentType.USER, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_UpdateOther_AdminUser() throws JsonGenerationException,
			JsonMappingException, IOException {

		Response r = sendUpdate(getOtherTestResourcePath(),
				getOtherTestResource(), TestAccessTokenFactory.getAdminAgentAT(userAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.UPD_OTHER,
				AgentType.USER, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}

	//
	// UPDATE TESTS WITH SERVICE AGENT
	//
	@Test
	public void test_UpdateOwn_RegisteredService()
			throws JsonGenerationException, JsonMappingException, IOException {
		
		Response r = sendUpdate(getTestResourcePath(), getTestResource(),
				TestAccessTokenFactory.getRegisteredAgentAT(serviceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.UPD_OWN,
				AgentType.SERVICE, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_UpdateOther_RegisteredService()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendUpdate(getOtherTestResourcePath(),
				getOtherTestResource(), TestAccessTokenFactory.getRegisteredAgentAT(serviceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.UPD_OTHER,
				AgentType.SERVICE, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_UpdateOwn_AdminService() throws JsonGenerationException,
			JsonMappingException, IOException {

		Response r = sendUpdate(getTestResourcePath(), getTestResource(),
				TestAccessTokenFactory.getAdminAgentAT(serviceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.UPD_OWN,
				AgentType.SERVICE, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_UpdateOther_AdminService() throws JsonGenerationException,
			JsonMappingException, IOException {

		Response r = sendUpdate(getOtherTestResourcePath(),
				getOtherTestResource(), TestAccessTokenFactory.getAdminAgentAT(serviceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.UPD_OTHER,
				AgentType.SERVICE, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}

	//
	// UPDATE TESTS WITH SERVICE INSTANCE AGENT
	//
	@Test
	public void test_UpdateOwn_RegisteredServiceInstance()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendUpdate(getTestResourcePath(), getTestResource(),
				TestAccessTokenFactory.getRegisteredAgentAT(serviceInstanceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.UPD_OWN,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_UpdateOther_RegisteredServiceInstance()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendUpdate(getOtherTestResourcePath(),
				getOtherTestResource(),
				TestAccessTokenFactory.getRegisteredAgentAT(serviceInstanceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.UPD_OTHER,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_UpdateOwn_AdminServiceInstance()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendUpdate(getTestResourcePath(), getTestResource(),
				TestAccessTokenFactory.getAdminAgentAT(serviceInstanceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.UPD_OWN,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_UpdateOther_AdminServiceInstance()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendUpdate(getOtherTestResourcePath(),
				getOtherTestResource(), TestAccessTokenFactory.getAdminAgentAT(serviceInstanceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.UPD_OTHER,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}

	//
	// UNAUTHORIZED DELETE TESTS
	//
	@Test
	public void test_Delete_WithoutAccessToken()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendDelete(getTestResourcePath(), null);

		assertEquals(401, r.getStatus());

	}

	@Test
	public void test_Delete_WithInvalidAccessToken()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendDelete(getTestResourcePath(), INVALID_AT);

		assertEquals(401, r.getStatus());

	}

	@Test
	public void test_Delete_WithUnauthenticAccessToken()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendDelete(getTestResourcePath(), INAUTHENTIC_AT);

		assertEquals(401, r.getStatus());

	}

	//
	// DELETE TESTS RESOURCE NOT FOUND
	//
	@Test
	public void test_DeleteNotFound() throws JsonGenerationException,
			JsonMappingException, IOException {

		String invalidPath = getTestResourcePath();
		invalidPath = invalidPath
				.substring(0, invalidPath.lastIndexOf("/") + 1)
				+ "123456789012345678901234";
		Response r = sendDelete(invalidPath, USER_ADMIN_AT);

		assertEquals(404, r.getStatus());
	}

	//
	// DELETE TESTS WITH USER AGENT
	//
	@Test
	public void test_DeleteOwn_RegisteredUser() throws JsonGenerationException,
			JsonMappingException, IOException {

		Response r = sendDelete(getTestResourcePath(),
				TestAccessTokenFactory.getRegisteredAgentAT(userAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.DEL_OWN,
				AgentType.USER, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_DeleteOther_RegisteredUser()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendDelete(getOtherTestResourcePath(),
				TestAccessTokenFactory.getRegisteredAgentAT(userAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.DEL_OTHER,
				AgentType.USER, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_DeleteOwn_AdminUser() throws JsonGenerationException,
			JsonMappingException, IOException {

		Response r = sendDelete(getTestResourcePath(),
				TestAccessTokenFactory.getAdminAgentAT(userAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.DEL_OWN,
				AgentType.USER, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_DeleteOther_AdminUser() throws JsonGenerationException,
			JsonMappingException, IOException {
		Response r = sendDelete(getOtherTestResourcePath(),
				TestAccessTokenFactory.getAdminAgentAT(userAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.DEL_OTHER,
				AgentType.USER, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}

	//
	// DELETE TESTS WITH SERVICE AGENT
	//
	@Test
	public void test_DeleteOwn_RegisteredService()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response r = sendDelete(getTestResourcePath(),
				TestAccessTokenFactory.getRegisteredAgentAT(serviceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.DEL_OWN,
				AgentType.SERVICE, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_DeleteOther_RegisteredService()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendDelete(getOtherTestResourcePath(),
				TestAccessTokenFactory.getRegisteredAgentAT(serviceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.DEL_OTHER,
				AgentType.SERVICE, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_DeleteOwn_AdminService() throws JsonGenerationException,
			JsonMappingException, IOException {

		Response r = sendDelete(getTestResourcePath(),
				TestAccessTokenFactory.getAdminAgentAT(serviceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.DEL_OWN,
				AgentType.SERVICE, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_DeleteOther_AdminService() throws JsonGenerationException,
			JsonMappingException, IOException {

		Response r = sendDelete(getOtherTestResourcePath(),
				TestAccessTokenFactory.getAdminAgentAT(serviceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.DEL_OTHER,
				AgentType.SERVICE, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}

	//
	// DELETE TESTS WITH SERVICE INSTANCE AGENT
	//
	@Test
	public void test_DeleteOwn_RegisteredServiceInstance()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendDelete(getTestResourcePath(),
				TestAccessTokenFactory.getRegisteredAgentAT(serviceInstanceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.DEL_OWN,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_DeleteOther_RegisteredServiceInstance()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendDelete(getOtherTestResourcePath(),
				TestAccessTokenFactory.getRegisteredAgentAT(serviceInstanceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.DEL_OTHER,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_DeleteOwn_AdminServiceInstance()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendDelete(getTestResourcePath(),
				TestAccessTokenFactory.getAdminAgentAT(serviceInstanceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.DEL_OWN,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_DeleteOther_AdminServiceInstance()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendDelete(getOtherTestResourcePath(),
				TestAccessTokenFactory.getAdminAgentAT(serviceInstanceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.DEL_OTHER,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}
}
