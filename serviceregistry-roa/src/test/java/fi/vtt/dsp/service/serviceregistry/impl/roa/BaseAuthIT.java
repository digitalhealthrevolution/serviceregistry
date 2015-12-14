package fi.vtt.dsp.service.serviceregistry.impl.roa;

import fi.vtt.dsp.service.serviceregistry.TestProperty;
import fi.vtt.dsp.service.serviceregistry.TestProperties;
import fi.vtt.dsp.service.serviceregistry.TestData;
import fi.vtt.dsp.service.serviceregistry.auth.token.AccessToken;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.MessageProcessingException;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.http.MediaType;

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.UserGroup;
import fi.vtt.dsp.service.serviceregistry.common.UserProfile;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;

public abstract class BaseAuthIT {

	protected static final String PATH_SERVICES = "serviceregistrations";
	protected static final String PATH_SERVICE = PATH_SERVICES + "/%s";
	protected static final String PATH_USERGROUPS = "usergroups";
	protected static final String PATH_USERGROUP = PATH_USERGROUPS + "/%s";
	protected static final String PATH_SERVICE_INSTANCES = PATH_SERVICE + "/serviceinstances";
	protected static final String PATH_SERVICE_INSTANCE = PATH_SERVICE_INSTANCES + "/%s";
	protected static final String PATH_USERS = "users";
	protected static final String PATH_USER = PATH_USERS + "/%s";

	protected static final ObjectMapper MAPPER = new ObjectMapper();
	
	protected static final AccessToken USER_ADMIN_AT;	
	protected static final AccessToken INVALID_AT;	
	protected static final AccessToken INAUTHENTIC_AT;	
	
	private static String serviceRegistryBaseURL;

	protected UserProfile userAgent;
	protected ServiceRegistryEntry serviceAgent;
	protected ServiceInstance serviceInstanceAgent;

	protected UserProfile otherUserProfile;
	protected ServiceRegistryEntry otherService;
	protected ServiceInstance otherServiceInstance;
	
	static {
		USER_ADMIN_AT = TestAccessTokenFactory.getAdminUserAt("123456");
		INVALID_AT = TestAccessTokenFactory.getInvalidAccessToken();
		INAUTHENTIC_AT = TestAccessTokenFactory.getInauthenticAccessToken();
	}

	@BeforeClass
	public static void setupServiceUrl() {
		serviceRegistryBaseURL = TestProperties.get(TestProperty.SERVICE_URL);
		serviceRegistryBaseURL += "/resourcedirectory/v1/";
	}

	@Before
	public void setupTestAgents() throws JsonGenerationException, JsonMappingException, IOException {

		// create test user profiles
		userAgent = createUserProfile();
		otherUserProfile = createUserProfile();

		// create test services
		serviceAgent = createService(userAgent.getUserId());
		otherService = createService(otherUserProfile.getUserId());

		// create test service instances
		serviceInstanceAgent = createServiceInstance(userAgent.getUserId(), serviceAgent.getServiceId());
		otherServiceInstance = createServiceInstance(otherUserProfile.getUserId(), otherService.getServiceId());
	}

	@After
	public void cleanTestAgents() {
		if (userAgent.getUserId() != null) {
			deleteProfile(userAgent.getUserId());
		}
		if (serviceAgent.getServiceId() != null) {
			// removes also serviceInstanceAgent
			deleteService(serviceAgent.getServiceId());
		}

		if (otherUserProfile.getUserId() != null) {
			deleteProfile(otherUserProfile.getUserId());
		}
		if (otherService.getServiceId() != null) {
			// removes also serviceInstanceAgent
			deleteService(otherService.getServiceId());
		}
	}


	protected static WebClient setupJSONClient(String path) {
		List<Object> providers = new ArrayList();
		providers.add(new JacksonJaxbJsonProvider());
		WebClient client = WebClient.create(serviceRegistryBaseURL, providers);
		client.path(path);
		client.type(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE);
		return client;
	}

	protected static WebClient setupJSONClient(String path, AccessToken token) {
		List<Object> providers = new ArrayList();
		providers.add(new JacksonJaxbJsonProvider());
		WebClient client = WebClient.create(serviceRegistryBaseURL, providers,
				token.getBasicAuthUsername(), token.getBasicAuthPassword(),
				null);
		client.path(path);
		client.type(MediaType.APPLICATION_JSON_VALUE).accept(
				MediaType.APPLICATION_JSON_VALUE);
		return client;
	}

	protected static String getCreatedId(Response r) {
		String loc = r.getHeaderString("Location");
		String[] splittedUrl = loc.split("\\/");
		String id = splittedUrl[splittedUrl.length - 1];
		return id;
	}

	protected void deleteResource(String path) {
		WebClient client = setupJSONClient(path, USER_ADMIN_AT);
		client.delete();
	}

	protected static ServiceRegistryEntry getService(String serviceId)
			throws JsonParseException, JsonMappingException,
			MessageProcessingException, IllegalStateException, IOException {
		WebClient client = setupJSONClient(String.format(PATH_SERVICE, serviceId), USER_ADMIN_AT);
		Response r = client.get();
		return MAPPER.readValue(r.readEntity(String.class), ServiceRegistryEntry.class);
	}

	protected static UserGroup getUserGroup(String userGroupId)
			throws JsonParseException, JsonMappingException,
			MessageProcessingException, IllegalStateException, IOException {
		WebClient client = setupJSONClient(String.format(PATH_USERGROUP, userGroupId), USER_ADMIN_AT);
		Response r = client.get();
		return MAPPER.readValue(r.readEntity(String.class), UserGroup.class);
	}

	protected static UserGroup createUserGroup(UserGroup userGroup)
			throws JsonGenerationException, JsonMappingException, IOException {
		WebClient client = setupJSONClient(PATH_USERGROUPS, USER_ADMIN_AT);
		Response r = client.post(MAPPER.writeValueAsString(userGroup));
		assertEquals(201, r.getStatus());
		String userGroupId = getCreatedId(r);

		userGroup.setUserGroupId(userGroupId);

		return userGroup;
	}

	protected static ServiceRegistryEntry createService(String userId)
			throws JsonGenerationException, JsonMappingException, IOException {
		ServiceRegistryEntry service = TestData.getServiceRegistryEntry();
		WebClient client = setupJSONClient(PATH_SERVICES, USER_ADMIN_AT);
		service.getServiceDescription().setCreatedByUserId(userId);
		Response r = client.post(MAPPER.writeValueAsString(service));
		assertEquals(201, r.getStatus());
		String serviceId = getCreatedId(r);

		return getService(serviceId);
	}

	protected static ServiceRegistryEntry updateService(
			ServiceRegistryEntry service) throws JsonGenerationException,
			JsonMappingException, IOException {
		if (service.getServiceId() == null) {
			throw new IllegalArgumentException(
					"Cannot update service. Service.serviceId must be set.");
		}
		WebClient client = setupJSONClient(
				String.format(PATH_SERVICE, service.getServiceId()),
				USER_ADMIN_AT);
		Response r = client.put(MAPPER.writeValueAsString(service));
		assertEquals(200, r.getStatus());

		return getService(service.getServiceId());
	}

	protected static void deleteService(String serviceId) {
		WebClient client = setupJSONClient(
				String.format(PATH_SERVICE, serviceId), USER_ADMIN_AT);
		client.delete();
	}

	protected static void deleteUserGroup(String userGroupId) {
		WebClient client = setupJSONClient(
				String.format(PATH_USERGROUP, userGroupId), USER_ADMIN_AT);
		client.delete();
	}

	protected static ServiceInstance getServiceInstance(String serviceId,
			String serviceInstanceId) throws JsonParseException,
			JsonMappingException, MessageProcessingException,
			IllegalStateException, IOException {
		WebClient client = setupJSONClient(String.format(PATH_SERVICE_INSTANCE,
				serviceId, serviceInstanceId), USER_ADMIN_AT);
		Response r = client.get();
		return MAPPER.readValue(r.readEntity(String.class),
				ServiceInstance.class);
	}

	protected static ServiceInstance createServiceInstance(String userId,
			String serviceId) throws JsonGenerationException,
			JsonMappingException, IOException {
		ServiceInstance inst = TestData.getServiceInstance();
		WebClient client = setupJSONClient(
				String.format(PATH_SERVICE_INSTANCES, serviceId), USER_ADMIN_AT);
		inst.setCreatedByUserId(userId);
		Response r = client.post(MAPPER.writeValueAsString(inst));
		assertEquals(201, r.getStatus());
		String serviceInstanceId = getCreatedId(r);

		return getServiceInstance(serviceId, serviceInstanceId);
	}

	protected static ServiceInstance updateServiceInstance(
			ServiceRegistryEntry service, ServiceInstance inst)
			throws JsonGenerationException, JsonMappingException, IOException {
		if (service.getServiceId() == null) {
			throw new IllegalArgumentException("Cannot update service instance. Service.serviceId must be set.");
		}
		if (inst.getServiceInstanceId() == null) {
			throw new IllegalArgumentException("Cannot update service instance. ServiceInstance.serviceInstanceId must be set.");
		}
		WebClient client = setupJSONClient(String.format(PATH_SERVICE_INSTANCE,
				service.getServiceId(), inst.getServiceInstanceId()),
				USER_ADMIN_AT);
		Response r = client.put(MAPPER.writeValueAsString(inst));
		assertEquals(200, r.getStatus());

		return getServiceInstance(service.getServiceId(), inst.getServiceInstanceId());
	}

	protected static void deleteServiceInstance(String serviceId,
			String serviceInstanceId) {
		WebClient client = setupJSONClient(String.format(PATH_SERVICE_INSTANCE,
				serviceId, serviceInstanceId), USER_ADMIN_AT);
		client.delete();
	}

	protected static UserProfile createUserProfile()
			throws JsonGenerationException, JsonMappingException, IOException {
		UserProfile profile = TestData.getUserProfile();
		WebClient client = setupJSONClient(PATH_USERS, USER_ADMIN_AT);
		Response r = client.post(MAPPER.writeValueAsString(profile));
		assertEquals(201, r.getStatus());
		String userId = getCreatedId(r);
		profile.setUserId(userId);

		return profile;
	}

	protected static void deleteProfile(String userId) {
		WebClient client = setupJSONClient(String.format(PATH_USER, userId),
				USER_ADMIN_AT);
		client.delete();
	}

}
