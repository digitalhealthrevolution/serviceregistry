package fi.vtt.dsp.service.serviceregistry.impl.roa.functional;

import fi.vtt.dsp.service.serviceregistry.common.GroupRole;
import fi.vtt.dsp.service.serviceregistry.common.UserGroup;
import fi.vtt.dsp.service.serviceregistry.impl.handlers.BindingsGranter;
import fi.vtt.dsp.service.serviceregistry.impl.roa.BaseAuthIT;
import fi.vtt.dsp.service.serviceregistry.impl.roa.TestAccessTokenFactory;
import fi.vtt.dsp.serviceframework.common.Binding;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BindingKeyIT extends BaseAuthIT {
	private static final Logger LOGGER = Logger.getLogger(BindingKeyIT.class.getName());
	Binding binding = new Binding();
	UserGroup userGroup = null;

	@Before
	public void setUpTest() {
		binding.setBoundByServiceId(serviceAgent.getServiceId());
		binding.setBoundByServiceInstanceId(serviceInstanceAgent.getServiceInstanceId());
		binding.setRequestedByUserId(userAgent.getUserId());
		binding.setAuthorizedByUserId(otherUserProfile.getUserId());

		WebClient client = setupJSONClient("serviceregistrations/"
			+ otherService.getServiceId()
			+ "/serviceinstances/"
			+ otherServiceInstance.getServiceInstanceId()
			+ "/serviceaccessendpoint/bindings", TestAccessTokenFactory.getAdminAgentAT(otherUserProfile));

		Response r = client.post(binding);
		Assert.assertEquals(201, r.getStatus());

		String bindingURI = r.getLocation().toString();
		String bindingID = bindingURI.substring(bindingURI.lastIndexOf("/") + 1);
		binding.setBindingId(bindingID);
	}

	@Test
	public void test_otherProfileGetNotGranted() {
		WebClient client = setupJSONClient("serviceregistrations/"
			+ otherService.getServiceId()
			+ "/serviceinstances/"
			+ otherServiceInstance.getServiceInstanceId()
			+ "/serviceaccessendpoint/bindings/"
			+ binding.getBindingId()
			+ "/key", TestAccessTokenFactory.getRegisteredAgentAT(userAgent));

		Response response = client.get();

		Assert.assertEquals(403, response.getStatus());
	}

	@Test
	public void test_ownerProfileGetNotGranted() {
		WebClient client = setupJSONClient("serviceregistrations/"
			+ otherService.getServiceId()
			+ "/serviceinstances/"
			+ otherServiceInstance.getServiceInstanceId()
			+ "/serviceaccessendpoint/bindings/"
			+ binding.getBindingId()
			+ "/key", TestAccessTokenFactory.getRegisteredAgentAT(otherUserProfile));

		Response response = client.get();

		Assert.assertEquals(200, response.getStatus());
	}

	@Test
	public void test_otherProfileGetGranted() {
		haveBindingGranted();

		WebClient client = setupJSONClient("serviceregistrations/"
			+ otherService.getServiceId()
			+ "/serviceinstances/"
			+ otherServiceInstance.getServiceInstanceId()
			+ "/serviceaccessendpoint/bindings/"
			+ binding.getBindingId()
			+ "/key", TestAccessTokenFactory.getRegisteredAgentAT(userAgent));

		Response response = client.get();
		String responseBody = response.readEntity(String.class);

		Assert.assertEquals(200, response.getStatus());
		Assert.assertEquals(BindingsGranter.getVerificationCode(otherUserProfile.getUserId(), binding.getBindingId()), responseBody);
	}

	@Test
	public void test_inGroupProfileGetNotGranted() {
		userGroup = new UserGroup();
		GroupRole groupRole = new GroupRole();
		String postJSONString = null;

		groupRole.setUserId(userAgent.getUserId());
		groupRole.setAccessRights("write");

		userGroup.getGroupRole().add(groupRole);
		userGroup.getServiceRegistryEntryId().add(otherService.getServiceId());

		WebClient client = setupJSONClient("usergroups", TestAccessTokenFactory.getAdminAgentAT(userAgent));

		try {
			postJSONString = MAPPER.writeValueAsString(userGroup);
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error mapping user-group", e);
		}

		Response response = client.post(postJSONString);

		Assert.assertEquals(201, response.getStatus());

		userGroup.setUserGroupId(response.getLocation().toString().substring(response.getLocation().toString().lastIndexOf("/") + 1));
		
		client = setupJSONClient("serviceregistrations/"
			+ otherService.getServiceId()
			+ "/serviceinstances/"
			+ otherServiceInstance.getServiceInstanceId()
			+ "/serviceaccessendpoint/bindings/"
			+ binding.getBindingId()
			+ "/key", TestAccessTokenFactory.getRegisteredAgentAT(userAgent));

		response = client.get();

		Assert.assertEquals(200, response.getStatus());		
	}

	public void haveBindingGranted() {
		WebClient client = setupJSONClient("serviceregistrations/"
			+ otherService.getServiceId()
			+ "/serviceinstances/"
			+ otherServiceInstance.getServiceInstanceId()
			+ "/serviceaccessendpoint/bindings/"
			+ binding.getBindingId()
			+ "/grant/true/verification/"
			+ BindingsGranter.getVerificationCode(otherUserProfile.getUserId(), binding.getBindingId()),
			TestAccessTokenFactory.getRegisteredAgentAT(otherUserProfile));

		Response response = client.get();
		Assert.assertEquals(200, response.getStatus());
	}
	
	@After
	public void cleanUp() {
		if (userGroup != null) {
			WebClient client = setupJSONClient("usergroups/" + userGroup.getUserGroupId(), TestAccessTokenFactory.getAdminAgentAT(userAgent));
			client.delete();
		}
	}
}
