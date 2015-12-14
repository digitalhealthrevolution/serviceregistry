package fi.vtt.dsp.service.serviceregistry.impl.roa.auth;


import javax.ws.rs.core.Response;


import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.description.HumanReadableDescription;
import fi.vtt.dsp.service.serviceregistry.impl.roa.StaticResourceAuthIT;

public class HumanReadableDescriptionResourceAuthIT
		extends
			StaticResourceAuthIT<HumanReadableDescription> {

	public HumanReadableDescriptionResourceAuthIT() {

		// set expected responses
		setExpectedTestReqStatus(TestReq.UPD_OWN, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.OK);
		setExpectedTestReqStatus(TestReq.UPD_OWN, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.OK);
		setExpectedTestReqStatus(TestReq.UPD_OWN, AgentType.SERVICE_INSTANCE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
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
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.DEL_OTHER, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.DEL_OTHER, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.DEL_OTHER, AgentType.SERVICE_INSTANCE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
	}

	private String getResourcePath(ServiceRegistryEntry service) {
		return String.format(PATH_SERVICE
				+ "/servicedescription/humanreadabledescription",
				service.getServiceId());
	}

	@Override
	protected HumanReadableDescription getTestResource() {
		return serviceAgent.getServiceDescription()
				.getHumanReadableDescription();
	}

	@Override
	protected HumanReadableDescription getOtherTestResource() {
		return otherService.getServiceDescription()
				.getHumanReadableDescription();
	}

	@Override
	protected String getTestResourcePath() {
		return getResourcePath(serviceAgent);
	}

	@Override
	protected String getOtherTestResourcePath() {
		return getResourcePath(otherService);
	}
}
