package fi.vtt.dsp.service.serviceregistry.impl.roa.auth;

import fi.vtt.dsp.service.serviceregistry.TestData;
import javax.ws.rs.core.Response;

import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.impl.roa.ResourceCollectionAuthIT;

public class ServiceInstancesResourceAuthIT
		extends
			ResourceCollectionAuthIT<ServiceInstance> {

	public ServiceInstancesResourceAuthIT() {

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
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.CREATE_OTHER,
				AgentType.SERVICE_INSTANCE, AgentRole.ROLE_REGISTERED,
				Response.Status.FORBIDDEN);

	}

	@Override
	protected ServiceInstance getTestResource() {
		ServiceInstance inst = TestData.getServiceInstance();
		inst.setCreatedByUserId(userAgent.getUserId());
		return inst;
	}

	@Override
	protected void deleteTestResource(ServiceInstance resource) {
		deleteServiceInstance(serviceAgent.getServiceId(),
				resource.getServiceInstanceId());
	}

	@Override
	protected String getResourceCollectionPath() {
		return String.format(PATH_SERVICE_INSTANCES,
				serviceAgent.getServiceId());
	}

	@Override
	protected String getOtherResourceCollectionPath() {
		return String.format(PATH_SERVICE_INSTANCES,
				otherService.getServiceId());
	}

	@Override
	protected void setTestResourceId(ServiceInstance resource, String id) {
		resource.setServiceInstanceId(id);

	}

	@Override
	protected String getTestResourceId(ServiceInstance resource) {
		return resource.getServiceInstanceId();
	}

}
