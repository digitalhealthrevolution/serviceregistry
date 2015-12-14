package fi.vtt.dsp.service.serviceregistry.impl.roa.auth;

import fi.vtt.dsp.service.serviceregistry.TestData;
import javax.ws.rs.core.Response;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.impl.roa.ResourceCollectionAuthIT;

public class ServiceRegistrationsResourceAuthIT
		extends
			ResourceCollectionAuthIT<ServiceRegistryEntry> {

	public ServiceRegistrationsResourceAuthIT() {

		// expected response statuses
		setExpectedTestReqStatus(TestReq.CREATE_OWN, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.CREATED);
		setExpectedTestReqStatus(TestReq.CREATE_OWN, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.CREATED);
		setExpectedTestReqStatus(TestReq.CREATE_OWN,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_REGISTERED,
				Response.Status.FORBIDDEN);

		setExpectedTestReqStatus(TestReq.CREATE_OTHER, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.CREATED);
		setExpectedTestReqStatus(TestReq.CREATE_OTHER, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.CREATED);
		setExpectedTestReqStatus(TestReq.CREATE_OTHER,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_REGISTERED,
				Response.Status.FORBIDDEN);

	}

	@Override
	protected ServiceRegistryEntry getTestResource() {
		ServiceRegistryEntry testService = TestData.getServiceRegistryEntry();
		testService.getServiceDescription().setCreatedByUserId(
				userAgent.getUserId());
		return testService;
	}

	@Override
	protected void deleteTestResource(ServiceRegistryEntry resource) {
		deleteService(resource.getServiceId());
	}

	@Override
	protected String getResourceCollectionPath() {
		return PATH_SERVICES;
	}

	@Override
	protected String getOtherResourceCollectionPath() {
		return PATH_SERVICES;
	}

	@Override
	protected void setTestResourceId(ServiceRegistryEntry resource, String id) {
		resource.setServiceId(id);

	}

	@Override
	protected String getTestResourceId(ServiceRegistryEntry resource) {
		return resource.getServiceId();
	}

}
