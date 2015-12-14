package fi.vtt.dsp.service.serviceregistry.impl.roa.auth;

import fi.vtt.dsp.service.serviceregistry.TestData;
import javax.ws.rs.core.Response;

import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.description.UserFeedback;
import fi.vtt.dsp.service.serviceregistry.impl.roa.ResourceCollectionAuthIT;

public class UserFeedbacksResourceAuthIT
		extends
			ResourceCollectionAuthIT<UserFeedback> {

	public UserFeedbacksResourceAuthIT() {

		// expected response statuses
		setExpectedTestReqStatus(TestReq.CREATE_OWN, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.CREATE_OWN, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
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

	private String getPath(ServiceRegistryEntry service) {
		return String.format(
				PATH_SERVICE + "/servicedescription/userfeedbacks",
				service.getServiceId());
	}

	@Override
	protected UserFeedback getTestResource() {
		return TestData.getUserFeedback(userAgent);
	}

	@Override
	protected void deleteTestResource(UserFeedback resource) {
		deleteResource(getPath(serviceAgent) + "/"
				+ resource.getUserFeedbackId());
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
	protected void setTestResourceId(UserFeedback resource, String id) {
		resource.setUserFeedbackId(id);

	}

	@Override
	protected String getTestResourceId(UserFeedback resource) {
		return resource.getUserFeedbackId();
	}

}
