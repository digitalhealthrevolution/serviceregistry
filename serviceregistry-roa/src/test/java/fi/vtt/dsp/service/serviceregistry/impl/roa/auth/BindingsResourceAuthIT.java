package fi.vtt.dsp.service.serviceregistry.impl.roa.auth;

import fi.vtt.dsp.service.serviceregistry.TestData;
import javax.ws.rs.core.Response;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.impl.roa.ResourceCollectionAuthIT;
import fi.vtt.dsp.serviceframework.common.Binding;

public class BindingsResourceAuthIT extends ResourceCollectionAuthIT<Binding> {

	public BindingsResourceAuthIT() {

		// expected response statuses
		setExpectedTestReqStatus(TestReq.CREATE_OWN, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.CREATED);
		setExpectedTestReqStatus(TestReq.CREATE_OWN, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.CREATE_OWN,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_REGISTERED,
				Response.Status.FORBIDDEN);

		setExpectedTestReqStatus(TestReq.CREATE_OTHER, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.CREATED);
		setExpectedTestReqStatus(TestReq.CREATE_OTHER, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.CREATED);
		setExpectedTestReqStatus(TestReq.CREATE_OTHER,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_REGISTERED,
				Response.Status.CREATED);

	}

	private String getPath(ServiceRegistryEntry service, ServiceInstance inst) {
		return String.format(PATH_SERVICE_INSTANCE
				+ "/serviceaccessendpoint/bindings", service.getServiceId(),
				inst.getServiceInstanceId());
	}

	@Override
	protected Binding getTestResource() {
		Binding binding = TestData.getBinding(serviceAgent,
				serviceInstanceAgent);
		binding.setAuthorizedByUserId(otherUserProfile.getUserId());
		return binding;
	}

	@Override
	protected void deleteTestResource(Binding resource) {
		deleteResource(getPath(serviceAgent, serviceInstanceAgent) + "/"
				+ resource.getBindingId());
	}

	@Override
	protected String getResourceCollectionPath() {
		return getPath(serviceAgent, serviceInstanceAgent);
	}

	@Override
	protected String getOtherResourceCollectionPath() {
		return getPath(otherService, otherServiceInstance);
	}

	@Override
	protected void setTestResourceId(Binding resource, String id) {
		resource.setBindingId(id);

	}

	@Override
	protected String getTestResourceId(Binding resource) {
		return resource.getBindingId();
	}
}
