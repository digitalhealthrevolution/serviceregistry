package fi.vtt.dsp.service.serviceregistry.impl.roa.auth;

import fi.vtt.dsp.service.serviceregistry.TestData;
import fi.vtt.dsp.service.serviceregistry.auth.token.AccessToken;
import javax.ws.rs.core.Response;

import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.common.GroupRole;
import fi.vtt.dsp.service.serviceregistry.common.UserGroup;
import fi.vtt.dsp.service.serviceregistry.common.UserProfile;
import fi.vtt.dsp.service.serviceregistry.impl.roa.ResourceCollectionAuthIT;
import fi.vtt.dsp.service.serviceregistry.impl.roa.TestAccessTokenFactory;
import java.io.IOException;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class UserProfilesResourceAuthIT
		extends
			ResourceCollectionAuthIT<UserProfile> {
    UserGroup userGroup = new UserGroup();
    
	public UserProfilesResourceAuthIT() {

		// expected response statuses
		setExpectedTestReqStatus(TestReq.CREATE_OWN, AgentType.USER,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
		setExpectedTestReqStatus(TestReq.CREATE_OWN, AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED, Response.Status.FORBIDDEN);
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

	@Override
	protected UserProfile getTestResource() {
		return TestData.getUserProfile();
	}

	@Override
	protected void deleteTestResource(UserProfile resource) {
		deleteProfile(resource.getUserId());
	}

	@Override
	protected void setTestResourceId(UserProfile resource, String id) {
		resource.setUserId(id);
	}

	@Override
	protected String getTestResourceId(UserProfile resource) {
		return resource.getUserId();
	}

	@Override
	protected String getResourceCollectionPath() {
		return PATH_USERS;
	}

	@Override
	protected String getOtherResourceCollectionPath() {
		return PATH_USERS;
	}
        
        @Test
        public void test_UnauthenticatedUser() {
            try {
                Response response = sendGet(getUserPath(userAgent), TestAccessTokenFactory.getGuestAgentAT(userAgent));
                Assert.assertEquals(200, response.getStatus());
            }
            catch (Exception e) {
                Assert.fail();
            }
        }
        
        @Test
        public void test_AuthenticatedGetSelf() {
            try {
                Response response = sendGet(getUserPath(userAgent), TestAccessTokenFactory.getRegisteredAgentAT(userAgent));
                Assert.assertEquals(200, response.getStatus());
            }
            catch (Exception e) {
                Assert.fail();
            }
        }
        
        @Test
        public void test_AuthenticatedUserGetOtherNotInGroup() {
            try {
                Response response = sendGet(getUserPath(userAgent), TestAccessTokenFactory.getRegisteredAgentAT(otherUserProfile));
                Assert.assertEquals(200, response.getStatus());
            }
            catch (Exception e) {
                Assert.fail();
            }
        }
        
        @Test
        public void test_AuthenticatedUserGetOtherInGroup() {            
            GroupRole groupRole = new GroupRole();
            GroupRole groupRole2 = new GroupRole();
            
            groupRole.setUserId(userAgent.getUserId());
            groupRole2.setUserId(otherUserProfile.getUserId());
            
            userGroup.getGroupRole().add(groupRole);
            userGroup.getGroupRole().add(groupRole2);
            
            try {
                userGroup = createUserGroup(userGroup);
            }
            catch (Exception e) {
                Assert.fail();
            }
            
            try {
                Response response = sendGet(getUserPath(userAgent), TestAccessTokenFactory.getRegisteredAgentAT(otherUserProfile));
                Assert.assertEquals(200, response.getStatus());
            }
            catch (Exception e) {
                Assert.fail();
            }
        }
        
        @After
        public void test_CleanUpGroup() {
            deleteUserGroup(userGroup.getUserGroupId());
        }
        
        private Response sendGet(String resourcePath, AccessToken token) throws JsonGenerationException, JsonMappingException, IOException {
            WebClient client;

            if (token != null) {
                client = setupJSONClient(resourcePath, token);
            } 
            else {
                client = setupJSONClient(resourcePath);
            }

            Response response = client.get();

            return response;
	}

        private String getUserPath(UserProfile userProfile) {
            return String.format(PATH_USER, userProfile.getUserId());
        }
}
