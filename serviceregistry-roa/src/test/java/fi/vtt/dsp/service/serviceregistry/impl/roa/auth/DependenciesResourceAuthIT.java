package fi.vtt.dsp.service.serviceregistry.impl.roa.auth;

import fi.vtt.dsp.service.serviceregistry.TestData;
import javax.ws.rs.core.Response;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.description.Dependency;
import fi.vtt.dsp.service.serviceregistry.impl.roa.ResourceCollectionAuthIT;

public class DependenciesResourceAuthIT
		extends
			ResourceCollectionAuthIT<Dependency> {

	public DependenciesResourceAuthIT() {

		// expected response statuses
		setExpectedTestReqStatus(TestReq.CREATE_OWN, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.CREATED);
		setExpectedTestReqStatus(TestReq.CREATE_OWN, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.CREATED);
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

	private String getPath(ServiceRegistryEntry service) {
		return String.format(PATH_SERVICE + "/servicedescription/dependencies",
				service.getServiceId());
	}

	@Override
	protected Dependency getTestResource() {
		return TestData.getDependency();
	}

	@Override
	protected void deleteTestResource(Dependency resource) {
		deleteResource(getPath(serviceAgent) + "/" + resource.getDependencyId());
	}

	@Override
	protected String getResourceCollectionPath() {
		return getPath(serviceAgent);
	}

	@Override
	protected String getOtherResourceCollectionPath() {
		return getPath(otherService);
	}

	@Override
	protected void setTestResourceId(Dependency resource, String id) {
		resource.setDependencyId(id);

	}

	@Override
	protected String getTestResourceId(Dependency resource) {
		return resource.getDependencyId();
	}

}
