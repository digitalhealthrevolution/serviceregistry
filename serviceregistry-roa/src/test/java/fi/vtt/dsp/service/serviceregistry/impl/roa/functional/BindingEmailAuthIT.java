package fi.vtt.dsp.service.serviceregistry.impl.roa.functional;

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.UserProfile;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.impl.handlers.BindingsGranter;
import fi.vtt.dsp.service.serviceregistry.impl.roa.BaseAuthIT;
import fi.vtt.dsp.service.serviceregistry.impl.roa.TestAccessTokenFactory;
import fi.vtt.dsp.serviceframework.common.Binding;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BindingEmailAuthIT extends BaseAuthIT {
	private static final Logger LOGGER = Logger.getLogger(BindingEmailAuthIT.class.getName());
	private UserProfile userProfile;
	private UserProfile otherProfile;
	private ServiceRegistryEntry serviceRegistryEntry;
	private ServiceRegistryEntry serviceRegistryEntry2;
	private ServiceInstance serviceInstance;
	private ServiceInstance serviceInstance2;
	private Binding binding = new Binding();
	
	@Before
	public void setUpTest() {
		try {
			LOGGER.log(Level.FINE, "Setting up test");
			
			userProfile = createUserProfile();
			otherProfile = createUserProfile();
			serviceRegistryEntry = createService(userProfile.getUserId());
			serviceRegistryEntry2 = createService(otherProfile.getUserId());
			serviceInstance = createServiceInstance(userProfile.getUserId(), serviceRegistryEntry.getServiceId());
			serviceInstance2 = createServiceInstance(otherProfile.getUserId(), serviceRegistryEntry2.getServiceId());			
			LOGGER.log(Level.FINE, "Users and services created");
			
			binding.setBoundByServiceId(serviceRegistryEntry.getServiceId());
			binding.setBoundByServiceInstanceId(serviceInstance.getServiceInstanceId());
			binding.setRequestedByUserId(userProfile.getUserId());
			binding.setAuthorizedByUserId(otherProfile.getUserId());

			LOGGER.log(Level.FINE, "Sending binding");
			WebClient client = setupJSONClient("serviceregistrations/"
				+ serviceRegistryEntry2.getServiceId()
				+ "/serviceinstances/"
				+ serviceInstance2.getServiceInstanceId()
				+ "/serviceaccessendpoint/bindings", TestAccessTokenFactory.getAdminAgentAT(userProfile));
			
			Response r = client.post(binding);
			LOGGER.log(Level.FINE, "Entity " + r.getEntity().toString());
			Assert.assertEquals(201, r.getStatus());
			LOGGER.log(Level.FINE, "Binding sent");
			
			String bindingURI = r.getLocation().toString();			
			String bindingID = bindingURI.substring(bindingURI.lastIndexOf("/") + 1);
			binding.setBindingId(bindingID);
			LOGGER.log(Level.FINE, "Binding ID: " + bindingID);
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error setting up binding-email-auth test", e);
			Assert.fail();
		}
	}
	
	@Test
	public void test_invalidVerificationCode() {
		try {
			LOGGER.log(Level.FINE, "Testing invalid verfication code");
			
			WebClient client = setupJSONClient("serviceregistrations/"
				+ serviceRegistryEntry2.getServiceId()
				+ "/serviceinstances/"
				+ serviceInstance2.getServiceInstanceId()
				+ "/serviceaccessendpoint/bindings/"
				+ binding.getBindingId()
				+ "/grant/true/verification/" + "1y23u12y3iu1y23iu1y23iuy12y3", TestAccessTokenFactory.getRegisteredAgentAT(otherProfile));
			
			Response r = client.get();
			Assert.assertEquals(403, r.getStatus());
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error testing invalid code", e);
			Assert.fail();
		}
	}
	
	@Test 
	public void test_validVerificationCodeGrant() {
		try {
			LOGGER.log(Level.FINE, "Testing valid code, grant");
			
			WebClient client = setupJSONClient("serviceregistrations/"
				+ serviceRegistryEntry2.getServiceId()
				+ "/serviceinstances/"
				+ serviceInstance2.getServiceInstanceId()
				+ "/serviceaccessendpoint/bindings/"
				+ binding.getBindingId()
				+ "/grant/true/verification/" + 
				BindingsGranter.getVerificationCode(otherProfile.getUserId(), binding.getBindingId()), 
				TestAccessTokenFactory.getRegisteredAgentAT(otherProfile));
			
			Response r = client.get();
			Assert.assertEquals(200, r.getStatus());
			
			LOGGER.log(Level.FINE, "Grant sent");
			
			client = setupJSONClient("serviceregistrations/"
				+ serviceRegistryEntry2.getServiceId()
				+ "/serviceinstances/"
				+ serviceInstance2.getServiceInstanceId()
				+ "/serviceaccessendpoint/bindings/"
				+ binding.getBindingId(), 
				TestAccessTokenFactory.getRegisteredAgentAT(otherProfile));
			
			r = client.get();			
			Assert.assertEquals(200, r.getStatus());
			
			LOGGER.log(Level.FINE, "Binding fetched again");
			
			String responseBody = r.readEntity(String.class);
			ObjectMapper mapper = new ObjectMapper();
			Binding responseBinding = mapper.readValue(responseBody, Binding.class);
		
			Assert.assertEquals(responseBinding.getAuthorizedByUserId(), otherProfile.getUserId());
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error testing valid code, grant", e);
			Assert.fail();
		}
	}
	
	@Test 
	public void test_validVerificationCodeDeny() {
		try {
			LOGGER.log(Level.FINE, "Testing valid code, deny");
			
			WebClient client = setupJSONClient("serviceregistrations/"
				+ serviceRegistryEntry2.getServiceId()
				+ "/serviceinstances/"
				+ serviceInstance2.getServiceInstanceId()
				+ "/serviceaccessendpoint/bindings/"
				+ binding.getBindingId()
				+ "/grant/false/verification/" + 
				BindingsGranter.getVerificationCode(otherProfile.getUserId(), binding.getBindingId()), 
				TestAccessTokenFactory.getRegisteredAgentAT(otherProfile));
			
			Response r = client.get();
			Assert.assertEquals(200, r.getStatus());
			
			LOGGER.log(Level.FINE, "Trying to fetch binding");
			
			client = setupJSONClient("serviceregistrations/"
				+ serviceRegistryEntry2.getServiceId()
				+ "/serviceinstances/"
				+ serviceInstance2.getServiceInstanceId()
				+ "/serviceaccessendpoint/bindings/"
				+ binding.getBindingId(),
				TestAccessTokenFactory.getRegisteredAgentAT(otherProfile));
			
			r = client.get();			

			Assert.assertEquals(404, r.getStatus());
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error testing valid code, deny", e);
			Assert.fail();
		}
	}	
	
	@After
	public void cleanUp() {
		try {
			deleteProfile(userProfile.getUserId());
			deleteProfile(otherProfile.getUserId());
			deleteService(serviceRegistryEntry.getServiceId());
			deleteService(serviceRegistryEntry2.getServiceId());
		}
		catch (Exception e) {
		}
	}		
}
