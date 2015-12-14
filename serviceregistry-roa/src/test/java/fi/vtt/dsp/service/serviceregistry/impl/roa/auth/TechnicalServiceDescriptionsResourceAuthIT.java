package fi.vtt.dsp.service.serviceregistry.impl.roa.auth;

import fi.vtt.dsp.service.serviceregistry.TestData;
import javax.ws.rs.core.Response;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.description.TechnicalServiceDescription;
import fi.vtt.dsp.service.serviceregistry.impl.roa.ResourceCollectionAuthIT;

public class TechnicalServiceDescriptionsResourceAuthIT
		extends
			ResourceCollectionAuthIT<TechnicalServiceDescription> {

	public TechnicalServiceDescriptionsResourceAuthIT() {

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
		return String.format(PATH_SERVICE
				+ "/servicedescription/technicaldescriptions",
				service.getServiceId());
	}

	@Override
	protected TechnicalServiceDescription getTestResource() {
		return TestData.getTechnicalServiceDescription();
	}

	@Override
	protected void deleteTestResource(TechnicalServiceDescription resource) {
		deleteResource(getPath(serviceAgent) + "/"
				+ resource.getTechnicalDescriptionId());
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
	protected void setTestResourceId(TechnicalServiceDescription resource,
			String id) {
		resource.setTechnicalDescriptionId(id);

	}

	@Override
	protected String getTestResourceId(TechnicalServiceDescription resource) {
		return resource.getTechnicalDescriptionId();
	}

}
