package fi.vtt.dsp.service.serviceregistry.impl.roa.auth;

import fi.vtt.dsp.service.serviceregistry.TestData;
import java.io.IOException;
import javax.ws.rs.core.Response;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.impl.roa.ResourceAuthIT;
import fi.vtt.dsp.service.serviceregistry.impl.roa.TestAccessTokenFactory;
import fi.vtt.dsp.serviceframework.common.Binding;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class BindingCreatedResourceAuthIT extends ResourceAuthIT<Binding> {
	private static final Logger LOGGER = Logger.getLogger(BindingCreatedResourceAuthIT.class.getName());

	public BindingCreatedResourceAuthIT() {

		// set expected responses
		setExpectedTestReqStatus(TestReq.UPD_OWN, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.OK);
		setExpectedTestReqStatus(TestReq.UPD_OWN, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.OK);
		setExpectedTestReqStatus(TestReq.UPD_OWN, AgentType.SERVICE_INSTANCE,
				AgentRole.ROLE_REGISTERED, Response.Status.OK);
		
		setExpectedTestReqStatus(TestReq.UPD_OTHER, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.UPD_OTHER, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.UPD_OTHER, AgentType.SERVICE_INSTANCE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);

		setExpectedTestReqStatus(TestReq.DEL_OWN, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.OK);
		setExpectedTestReqStatus(TestReq.DEL_OWN, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.OK);
		setExpectedTestReqStatus(TestReq.DEL_OWN, AgentType.SERVICE_INSTANCE,
				AgentRole.ROLE_REGISTERED, Response.Status.OK);
		
		setExpectedTestReqStatus(TestReq.DEL_OTHER, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.DEL_OTHER, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.DEL_OTHER, AgentType.SERVICE_INSTANCE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
	}

	private Binding ownBinding = null;
	private Binding otherBinding = null;

	private String getResourcePath(ServiceRegistryEntry service,
			ServiceInstance inst, Binding b) {
		return String.format(PATH_SERVICE_INSTANCE
				+ "/serviceaccessendpoint/bindings/%s", service.getServiceId(),
				inst.getServiceInstanceId(), b.getBindingId());
	}

	@Before
	public void setupCreatedBinding() throws JsonGenerationException,
			JsonMappingException, IOException {

		// setup own binding to other instance
		Binding ownBinding = TestData.getBinding(serviceAgent,
				serviceInstanceAgent);
		ownBinding.setAuthorizedByUserId(otherUserProfile.getUserId());
		otherServiceInstance.getServiceAccessEndPoint().getBinding()
				.add(ownBinding);
		otherServiceInstance = updateServiceInstance(otherService,
				otherServiceInstance);
		this.ownBinding = otherServiceInstance.getServiceAccessEndPoint()
				.getBinding().get(0);

		// setup other binding to other instance
		Binding otherBinding = TestData.getBinding(otherService,
				otherServiceInstance);
		otherBinding.setAuthorizedByUserId(otherUserProfile.getUserId());
		otherServiceInstance.getServiceAccessEndPoint().getBinding()
				.add(otherBinding);
		otherServiceInstance = updateServiceInstance(otherService,
				otherServiceInstance);
		for (Binding b : otherServiceInstance.getServiceAccessEndPoint()
				.getBinding()) {
			if (!b.getBindingId().equals(this.ownBinding.getBindingId())) {
				this.otherBinding = b;
				break;
			}
		}
	}

	@Override
	protected Binding getTestResource() {
		return ownBinding;
	}

	@Override
	protected Binding getOtherTestResource() {
		return otherBinding;
	}

	@Override
	protected String getTestResourcePath() {
		return getResourcePath(otherService, otherServiceInstance, ownBinding);
	}

	@Override
	protected String getOtherTestResourcePath() {
		return getResourcePath(otherService, otherServiceInstance, otherBinding);
	}
	
	@Test
	@Override
	public void test_UpdateOwn_RegisteredService()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response r = sendUpdate(getTestResourcePath(), getTestResource(),
				TestAccessTokenFactory.getRegisteredAgentAT(otherService));

		int expectedStatus = getExpectedTestReqStatus(TestReq.UPD_OWN,
				AgentType.SERVICE, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}
	
	@Test
	@Override
	public void test_UpdateOwn_RegisteredUser() throws JsonGenerationException,
			JsonMappingException, IOException {
		Response r = sendUpdate(getTestResourcePath(), getTestResource(),
				TestAccessTokenFactory.getRegisteredAgentAT(otherUserProfile));

		int expectedStatus = getExpectedTestReqStatus(TestReq.UPD_OWN,
				AgentType.USER, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}
	
	@Test
	@Override
	public void test_UpdateOwn_RegisteredServiceInstance()
			throws JsonGenerationException, JsonMappingException, IOException {
		Response r = sendUpdate(getTestResourcePath(), getTestResource(),
				TestAccessTokenFactory.getRegisteredAgentAT(otherServiceInstance));

		int expectedStatus = getExpectedTestReqStatus(TestReq.UPD_OWN,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_REGISTERED);
		assertEquals(expectedStatus, r.getStatus());
	}
	
	
}
