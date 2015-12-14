package fi.vtt.dsp.service.serviceregistry.impl.roa.functional;

import fi.vtt.dsp.service.serviceregistry.TestData;
import fi.vtt.dsp.service.serviceregistry.impl.roa.BaseAuthIT;
import fi.vtt.dsp.service.serviceregistry.impl.roa.TestAccessTokenFactory;
import fi.vtt.dsp.serviceframework.common.Binding;
import java.io.IOException;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OutgoingBindingsResourceIT extends BaseAuthIT {
	private static final Logger LOGGER = Logger.getLogger(OutgoingBindingsResourceIT.class.getName());
	
	@Before
	public void setBindingData() throws JsonGenerationException, JsonMappingException, IOException {
		Binding testBinding = TestData.getBinding(otherService, otherServiceInstance);
		testBinding.setAuthorizedByUserId(userAgent.getUserId());
		serviceInstanceAgent.getServiceAccessEndPoint().getBinding().add(testBinding);
		serviceInstanceAgent = updateServiceInstance(serviceAgent, serviceInstanceAgent);
	}
	
	@Test
	public void testBindings() {
		StringBuilder b = new StringBuilder();
		b.append("/serviceregistrations/");
		b.append(otherService.getServiceId());
		b.append("/serviceinstances/");
		b.append(otherServiceInstance.getServiceInstanceId());
		b.append("/outgoingbindings");
		WebClient webClient = setupJSONClient(b.toString(), TestAccessTokenFactory.getAdminAgentAT(userAgent));
		Response response  = webClient.get();
		
		Assert.assertEquals(200, response.getStatus());
		ObjectMapper mapper = new ObjectMapper();
		String responseBody = response.readEntity(String.class);
		
		try {
			OutgoingBinding[] bindings = mapper.readValue(responseBody, OutgoingBinding[].class);
			Assert.assertEquals(1, bindings.length);
		}
		catch (Exception e) {
			Assert.fail();
		}
		b = new StringBuilder();
		b.append("/serviceregistrations/");
		b.append(serviceAgent.getServiceId());
		b.append("/serviceinstances/");
		b.append(serviceInstanceAgent.getServiceInstanceId());
		b.append("/outgoingbindings");		
		webClient = setupJSONClient(b.toString(), TestAccessTokenFactory.getAdminAgentAT(userAgent));
		response  = webClient.get();
		
		Assert.assertEquals(200, response.getStatus());
		mapper = new ObjectMapper();
		responseBody = response.readEntity(String.class);
		
		try {
			OutgoingBinding[] bindings = mapper.readValue(responseBody, OutgoingBinding[].class);
			Assert.assertEquals(0, bindings.length);
		}
		catch (Exception e) {
			Assert.fail();
		}
	}
	
	private static class OutgoingBinding {
		
		private String serviceId;
		private String serviceInstanceId;
		private Binding binding;

		public String getServiceId() {
			return serviceId;
		}
		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}
		public String getServiceInstanceId() {
			return serviceInstanceId;
		}
		public void setServiceInstanceId(String serviceInstanceId) {
			this.serviceInstanceId = serviceInstanceId;
		}		
		public Binding getBinding() {
			return binding;
		}
		public void setBinding(Binding binding) {
			this.binding = binding;
		}
	}
}
