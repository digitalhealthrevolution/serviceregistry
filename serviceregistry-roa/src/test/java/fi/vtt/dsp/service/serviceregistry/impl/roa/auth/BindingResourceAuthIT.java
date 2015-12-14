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
import fi.vtt.dsp.serviceframework.common.Binding;

public class BindingResourceAuthIT extends ResourceAuthIT<Binding> {

	public BindingResourceAuthIT() {

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

	private String getResourcePath(ServiceRegistryEntry service,
			ServiceInstance inst) {
		return String.format(PATH_SERVICE_INSTANCE
				+ "/serviceaccessendpoint/bindings/%s", service.getServiceId(),
				inst.getServiceInstanceId(), inst.getServiceAccessEndPoint()
						.getBinding().get(0).getBindingId());
	}

	@Before
	public void setBindingData() throws JsonGenerationException,
			JsonMappingException, IOException {

		// add other binding to own service instance
		Binding testBinding = TestData.getBinding(otherService,
				otherServiceInstance);
		testBinding.setAuthorizedByUserId(userAgent.getUserId());
		serviceInstanceAgent.getServiceAccessEndPoint().getBinding()
				.add(testBinding);
		serviceInstanceAgent = updateServiceInstance(serviceAgent,
				serviceInstanceAgent);

		// add other binding to other service
		Binding otherTestBinding = TestData.getBinding(otherService,
				otherServiceInstance);
		otherTestBinding.setAuthorizedByUserId(otherUserProfile.getUserId());
		otherServiceInstance.getServiceAccessEndPoint().getBinding()
				.add(otherTestBinding);
		otherServiceInstance = updateServiceInstance(otherService,
				otherServiceInstance);
	}

	@Override
	protected Binding getTestResource() {
		return serviceInstanceAgent.getServiceAccessEndPoint().getBinding()
				.get(0);
	}

	@Override
	protected Binding getOtherTestResource() {
		return otherServiceInstance.getServiceAccessEndPoint().getBinding()
				.get(0);
	}

	@Override
	protected String getTestResourcePath() {
		return getResourcePath(serviceAgent, serviceInstanceAgent);
	}

	@Override
	protected String getOtherTestResourcePath() {
		return getResourcePath(otherService, otherServiceInstance);
	}

}
