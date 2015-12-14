package fi.vtt.dsp.service.serviceregistry.impl.roa;

import fi.vtt.dsp.service.serviceregistry.auth.token.AccessToken;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import static org.junit.Assert.assertEquals;

public abstract class ResourceCollectionAuthIT<T> extends BaseAuthIT {

	private T testResource;

	protected static enum TestReq {

		CREATE_OWN, CREATE_OTHER,
	};

	private final Map<String, Integer> expectedResponses = new HashMap<>();

	public ResourceCollectionAuthIT() {

		// default response statuses
		setExpectedTestReqStatus(TestReq.CREATE_OWN, AgentType.USER,
				AgentRole.ROLE_ADMIN, Response.Status.CREATED);
		setExpectedTestReqStatus(TestReq.CREATE_OWN, AgentType.SERVICE,
				AgentRole.ROLE_ADMIN, Response.Status.CREATED);
		setExpectedTestReqStatus(TestReq.CREATE_OWN,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_ADMIN,
				Response.Status.CREATED);
		setExpectedTestReqStatus(TestReq.CREATE_OTHER, AgentType.USER,
				AgentRole.ROLE_ADMIN, Response.Status.CREATED);
		setExpectedTestReqStatus(TestReq.CREATE_OTHER, AgentType.SERVICE,
				AgentRole.ROLE_ADMIN, Response.Status.CREATED);
		setExpectedTestReqStatus(TestReq.CREATE_OTHER,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_ADMIN,
				Response.Status.CREATED);

		setExpectedTestReqStatus(TestReq.CREATE_OWN, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.CREATE_OWN, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.CREATE_OWN,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_REGISTERED,
				Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.CREATE_OTHER, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.CREATE_OTHER, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.CREATE_OTHER,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_REGISTERED,
				Response.Status.FORBIDDEN);

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

	@Before
	public void setupTestResource() {
		testResource = getTestResource();
	}

	@After
	public void deleteTestResource() {
		if (getTestResourceId(testResource) != null) {
			deleteTestResource(testResource);
		}
	}

	protected abstract T getTestResource();

	protected abstract void deleteTestResource(T resource);

	protected abstract void setTestResourceId(T resource, String id);

	protected abstract String getTestResourceId(T resource);

	protected abstract String getResourceCollectionPath();

	protected abstract String getOtherResourceCollectionPath();

	protected Response sendCreate(String resourceCollectionPath, AccessToken token)
			throws JsonGenerationException, JsonMappingException, IOException {

		WebClient client = null;

		if (token != null) {
			client = setupJSONClient(resourceCollectionPath, token);
		} else {
			client = setupJSONClient(resourceCollectionPath);
		}

		Response r = client.post(MAPPER.writeValueAsString(testResource));
		if (r.getStatus() == Response.Status.CREATED.getStatusCode()) {
			setTestResourceId(testResource, getCreatedId(r));
		}

		return r;
	}

	//
	// CREATE REQUEST TESTS
	//
	@Test
	public void test_Create_WithoutAccessToken()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendCreate(getResourceCollectionPath(), null);

		assertEquals(401, r.getStatus());

	}

	@Test
	public void test_Create_WithInvalidAccessToken()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendCreate(getResourceCollectionPath(), INVALID_AT);

		assertEquals(401, r.getStatus());

	}

	@Test
	public void test_Create_WithUnauthenticAccessToken()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendCreate(getResourceCollectionPath(), INAUTHENTIC_AT);

		assertEquals(401, r.getStatus());

	}

	//
	// USER
	//
	@Test
	public void test_CreateOwn_RegisteredUser() throws JsonGenerationException,
			JsonMappingException, IOException {

		Response r = sendCreate(getResourceCollectionPath(),
				TestAccessTokenFactory.getRegisteredAgentAT(userAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.CREATE_OWN,
				AgentType.USER, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_CreateOther_RegisteredUser()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendCreate(getOtherResourceCollectionPath(),
				TestAccessTokenFactory.getRegisteredAgentAT(userAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.CREATE_OTHER,
				AgentType.USER, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_Create_AdminUser() throws JsonGenerationException,
			JsonMappingException, IOException {

		Response r = sendCreate(getResourceCollectionPath(),
				TestAccessTokenFactory.getAdminAgentAT(userAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.CREATE_OWN,
				AgentType.USER, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_CreateOther_AdminUser() throws JsonGenerationException,
			JsonMappingException, IOException {

		Response r = sendCreate(getOtherResourceCollectionPath(),
				TestAccessTokenFactory.getAdminAgentAT(userAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.CREATE_OTHER,
				AgentType.USER, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}

	//
	// SERVICE
	//
	@Test
	public void test_CreateOwn_RegisteredService()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendCreate(getResourceCollectionPath(),
				TestAccessTokenFactory.getRegisteredAgentAT(serviceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.CREATE_OWN,
				AgentType.SERVICE, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_CreateOther_RegisteredService()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendCreate(getOtherResourceCollectionPath(),
				TestAccessTokenFactory.getRegisteredAgentAT(serviceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.CREATE_OTHER,
				AgentType.SERVICE, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_CreateOwn_AdminService() throws JsonGenerationException,
			JsonMappingException, IOException {

		Response r = sendCreate(getResourceCollectionPath(),
				TestAccessTokenFactory.getAdminAgentAT(serviceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.CREATE_OWN,
				AgentType.SERVICE, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_CreateOther_AdminService() throws JsonGenerationException,
			JsonMappingException, IOException {

		Response r = sendCreate(getOtherResourceCollectionPath(),
				TestAccessTokenFactory.getAdminAgentAT(serviceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.CREATE_OTHER,
				AgentType.SERVICE, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}

	//
	// SERVICE INSTANCE
	//
	@Test
	public void test_CreateOwn_RegisteredInstance()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendCreate(getResourceCollectionPath(),
				TestAccessTokenFactory.getRegisteredAgentAT(serviceInstanceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.CREATE_OWN,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_CreateOther_RegisteredInstance()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendCreate(getOtherResourceCollectionPath(),
				TestAccessTokenFactory.getRegisteredAgentAT(serviceInstanceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.CREATE_OTHER,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_CreateOwn_AdminInstance() throws JsonGenerationException,
			JsonMappingException, IOException {

		Response r = sendCreate(getResourceCollectionPath(),
				TestAccessTokenFactory.getAdminAgentAT(serviceInstanceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.CREATE_OWN,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}

	@Test
	public void test_CreateOther_AdminInstance()
			throws JsonGenerationException, JsonMappingException, IOException {

		Response r = sendCreate(getOtherResourceCollectionPath(),
				TestAccessTokenFactory.getAdminAgentAT(serviceInstanceAgent));

		int expectedStatus = getExpectedTestReqStatus(TestReq.CREATE_OTHER,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_ADMIN);
		assertEquals(expectedStatus, r.getStatus());
	}
}
